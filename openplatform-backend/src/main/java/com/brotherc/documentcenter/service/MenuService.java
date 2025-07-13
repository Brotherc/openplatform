package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.constants.DocCatalogGroupConstant;
import com.brotherc.documentcenter.constants.MenuConstant;
import com.brotherc.documentcenter.dao.MenuRepository;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.menu.*;
import com.brotherc.documentcenter.model.entity.Menu;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Mono<Menu> add(MenuAddDTO menuAddDTO) {
        // 校验排序值是否重复
        return menuRepository.countByParentIdAndSort(menuAddDTO.getParentId(), menuAddDTO.getSort().intValue())
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                    }
                    Menu menu = new Menu();
                    BeanUtils.copyProperties(menuAddDTO, menu);
                    menu.setSort(menuAddDTO.getSort().intValue());
                    menu.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                    menu.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    menu.setCreateTime(LocalDateTime.now());
                    menu.setUpdateTime(LocalDateTime.now());
                    return menuRepository.save(menu);
                });
    }

    public Mono<Page<MenuDTO>> page(MenuQueryDTO menuQueryDTO, Pageable pageable) {
        Criteria criteria = Criteria.empty();
        if (menuQueryDTO.getStatus() != null) {
            criteria = criteria.and(DocCatalogGroupConstant.STATUS).is(menuQueryDTO.getStatus());
        }
        if (StringUtils.isNotBlank(menuQueryDTO.getName())) {
            criteria = criteria.and(DocCatalogGroupConstant.NAME).like("%" + menuQueryDTO.getName() + "%");
        }

        Query query = Query.query(criteria)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .sort(pageable.getSort());

        return r2dbcEntityTemplate.select(Menu.class)
                .from(MenuConstant.MENU)
                .matching(query)
                .all()
                .map(o -> {
                    MenuDTO menuDTO = new MenuDTO();
                    BeanUtils.copyProperties(o, menuDTO);
                    return menuDTO;
                })
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(query, Menu.class))
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Menu> updateById(MenuUpdateDTO menuUpdateDTO) {
        // 校验排序值是否重复
        return menuRepository.findById(menuUpdateDTO.getMenuId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(original -> {
                    // 判断排序值是否发生变化
                    boolean sortChange = !Objects.equals(original.getParentId(), menuUpdateDTO.getParentId()) ||
                            !Objects.equals(menuUpdateDTO.getSort().intValue(), original.getSort());
                    // 如果排序值变化，校验排序值是否重复
                    Mono<Boolean> sortRepeat = sortChange ? menuRepository.countByParentIdAndSort(
                            menuUpdateDTO.getParentId(), menuUpdateDTO.getSort().intValue()
                    ).map(count -> count > 0) : Mono.just(false);

                    return sortRepeat.flatMap(repeat -> {
                        if (Objects.equals(true, repeat)) {
                            return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                        }

                        original.setName(menuUpdateDTO.getName());
                        original.setDescription(menuUpdateDTO.getDescription());
                        original.setParentId(menuUpdateDTO.getParentId());
                        original.setStatus(menuUpdateDTO.getStatus());
                        original.setPath(menuUpdateDTO.getPath());
                        original.setIcon(menuUpdateDTO.getIcon());
                        original.setSort(menuUpdateDTO.getSort().intValue());
                        original.setDocCatalogGroupId(menuUpdateDTO.getDocCatalogGroupId());
                        original.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                        original.setUpdateTime(LocalDateTime.now());

                        return menuRepository.save(original);
                    });
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(MenuDeleteDTO deleteDTO) {
        // 校验菜单下是否存在子菜单
        return menuRepository.countByParentId(deleteDTO.getMenuId())
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.MENU_CHILDREN_DELETE_ERROR));
                    }
                    return menuRepository.deleteById(deleteDTO.getMenuId());
                });
    }

    public Mono<List<MenuDTO>> getTree() {
        return menuRepository.findAll()
                .collectList()
                .map(list -> {
                    List<MenuDTO> dtos = list.stream().map(menu -> {
                                MenuDTO dto = new MenuDTO();
                                BeanUtils.copyProperties(menu, dto);
                                return dto;
                            })
                            .sorted(Comparator.comparing(MenuDTO::getSort, Comparator.nullsLast(Integer::compareTo)))
                            .collect(Collectors.toCollection(ArrayList::new));

                    return buildTree(
                            dtos,
                            0L,
                            MenuDTO::getMenuId,
                            MenuDTO::getParentId,
                            MenuDTO::setChildren,
                            Comparator.comparing(MenuDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                    );
                });
    }

    public Mono<List<MenuPortalDTO>> getTreePortal() {
        return menuRepository.findAll()
                .collectList()
                .map(list -> {
                    List<MenuPortalDTO> dtos = list.stream().map(menu -> {
                                MenuPortalDTO dto = new MenuPortalDTO();
                                BeanUtils.copyProperties(menu, dto);
                                dto.setKey(menu.getMenuId().toString());
                                dto.setParentKey(menu.getParentId().toString());
                                dto.setTitle(menu.getName());
                                if (menu.getDocCatalogGroupId() != null && StringUtils.isNotBlank(menu.getPath())) {
                                    String path = menu.getPath() + "?docCatalogGroupId=" + menu.getDocCatalogGroupId();
                                    dto.setPath(path);
                                }
                                return dto;
                            })
                            .sorted(Comparator.comparing(MenuPortalDTO::getSort, Comparator.nullsLast(Integer::compareTo)))
                            .collect(Collectors.toCollection(ArrayList::new));

                    return buildTree(
                            dtos,
                            "0",
                            MenuPortalDTO::getKey,
                            MenuPortalDTO::getParentKey,
                            MenuPortalDTO::setChildren,
                            Comparator.comparing(MenuPortalDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                    );
                });
    }

    public <T, ID> List<T> buildTree(
            List<T> all,
            ID rootParentId,
            Function<T, ID> idGetter,
            Function<T, ID> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter,
            Comparator<T> sorter
    ) {
        List<T> result = new ArrayList<>();

        // 排序
//        if (sorter != null) {
//            all.sort(sorter);
//        }

        for (T item : all) {
            ID parentId = parentIdGetter.apply(item);
            if (Objects.equals(parentId, rootParentId)) {
                List<T> children = buildTree(all, idGetter.apply(item), idGetter, parentIdGetter, childrenSetter, sorter);
                childrenSetter.accept(item, children);
                result.add(item);
            }
        }

        return result;
    }

    private List<MenuDTO> buildCatalogTree(List<Menu> all, Long parentId) {
        List<MenuDTO> result = new ArrayList<>();

        // 排序
        all.sort(Comparator.comparing(Menu::getSort, Comparator.nullsLast(Integer::compareTo)));

        List<MenuDTO> copyList = all.stream().map(o -> {
            MenuDTO menuDTO = new MenuDTO();
            BeanUtils.copyProperties(o, menuDTO);
            return menuDTO;
        }).toList();

        // 设置子节点
        for (MenuDTO item : copyList) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildCatalogTree(all, item.getMenuId()));
                result.add(item);
            }
        }

        return result;
    }

}
