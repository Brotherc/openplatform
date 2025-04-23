package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.dao.DocCatalogGroupRepository;
import com.brotherc.documentcenter.model.dto.doccataloggroup.*;
import com.brotherc.documentcenter.model.entity.DocCatalogGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class DocCatalogGroupService {

    @Autowired
    private DocCatalogGroupRepository docCatalogGroupRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<DocCatalogGroupDTO> getById(DocCatalogGroupDetailQueryDTO docCatalogGroupDetailQueryDTO) {
        return docCatalogGroupRepository.findById(docCatalogGroupDetailQueryDTO.getDocCatalogGroupId()).map(o -> {
            DocCatalogGroupDTO docCatalogGroupDTO = new DocCatalogGroupDTO();
            BeanUtils.copyProperties(o, docCatalogGroupDTO);
            return docCatalogGroupDTO;
        });
    }

    public Mono<Page<DocCatalogGroupDTO>> page(DocCatalogGroupQueryDTO docCatalogGroupQueryDTO, Pageable pageable) {
        Criteria criteria = Criteria.empty();

        if (docCatalogGroupQueryDTO.getStatus() != null) {
            criteria = criteria.and("status").is(docCatalogGroupQueryDTO.getStatus());
        }

        if (StringUtils.isNotBlank(docCatalogGroupQueryDTO.getName())) {
            criteria = criteria.and("name").like("%" + docCatalogGroupQueryDTO.getName() + "%");
        }

        Query query = Query.query(criteria)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .sort(pageable.getSort());

        return r2dbcEntityTemplate.select(DocCatalogGroup.class)
                .from("doc_catalog_group")
                .matching(query)
                .all()
                .map(o -> {
                    DocCatalogGroupDTO docCatalogGroupDTO = new DocCatalogGroupDTO();
                    BeanUtils.copyProperties(o, docCatalogGroupDTO);
                    return docCatalogGroupDTO;
                })
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(query, DocCatalogGroup.class))
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<DocCatalogGroup> add(DocCatalogGroupAddDTO docCatalogGroupAddDTO) {
        DocCatalogGroup docCatalogGroup = new DocCatalogGroup();
        BeanUtils.copyProperties(docCatalogGroupAddDTO, docCatalogGroup);
        docCatalogGroup.setStatus(1);
        docCatalogGroup.setSort(1);
        docCatalogGroup.setCreateBy(1L);
        docCatalogGroup.setUpdateBy(1L);
        docCatalogGroup.setCreateTime(LocalDateTime.now());
        docCatalogGroup.setUpdateTime(LocalDateTime.now());
        return docCatalogGroupRepository.save(docCatalogGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateById(DocCatalogGroupUpdateDTO docCatalogGroupUpdateDTO) {
        Query query = Query.query(Criteria.where("doc_catalog_group_id").is(docCatalogGroupUpdateDTO.getDocCatalogGroupId()));

        Update update = Update
                .update("name", docCatalogGroupUpdateDTO.getName())
                .set("description", docCatalogGroupUpdateDTO.getDescription())
                .set("updateTime", LocalDateTime.now())
                .set("updateBy", 1L);

        return r2dbcEntityTemplate.update(query, update, DocCatalogGroup.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(DocCatalogGroupDeleteDTO docCatalogGroupDeleteDTO) {
        return docCatalogGroupRepository.deleteById(docCatalogGroupDeleteDTO.getDocCatalogGroupId());
    }

}
