package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.helper.EsHelper;
import com.brotherc.documentcenter.model.dto.doccatalog.DocCatalogUpdateDTO;
import com.brotherc.documentcenter.model.entity.DocCatalog;
import com.brotherc.documentcenter.model.pojo.Document;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private EsHelper esHelper;

    public Mono<Void> add(DocCatalog docCatalog, String content) {
        Map<String, Object> esData = new HashMap<>();
        esData.put("id", docCatalog.getDocCatalogId());
        esData.put("name", docCatalog.getName());
        esData.put("docCatalogGroupId", docCatalog.getDocCatalogGroupId());
        esData.put("parentId", docCatalog.getParentId());
        esData.put("sort", docCatalog.getSort());
        esData.put("createTime", docCatalog.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        esData.put("updateTime", docCatalog.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        esData.put("createBy", docCatalog.getCreateBy());
        esData.put("updateBy", docCatalog.getUpdateBy());
        esData.put("isDel", docCatalog.getIsDel());
        esData.put("content", content);

        return esHelper.addData("document", docCatalog.getDocCatalogId().toString(), esData);
    }

    public Mono<Map<String, Object>> update(DocCatalogUpdateDTO updateDTO) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("name", updateDTO.getName());
        updateFields.put("parentId", updateDTO.getParentId());
        updateFields.put("sort", updateDTO.getSort().intValue());
        updateFields.put("updateTime", System.currentTimeMillis());
        updateFields.put("updateBy", 1);
        return esHelper.updateById("document", updateDTO.getDocCatalogId().toString(), updateFields);
    }

    public Mono<Document> getById(Long docCatalogId) {
        return esHelper.getById("document", docCatalogId.toString(), Document.class);
    }

    public Mono<Map<String, Object>> updateContentById(Long docCatalogId, String content) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("content", content);
        updateFields.put("updateTime", System.currentTimeMillis());
        updateFields.put("updateBy", 1);
        return esHelper.updateById("document", docCatalogId.toString(), updateFields);
    }

    public Mono<Void> deleteById(Long docCatalogId) {
        return esHelper.deleteById("document", docCatalogId.toString());
    }

    public Mono<Void> deleteByIdList(List<Long> docCatalogId) {
        return esHelper.deleteByIdList("document", docCatalogId.stream().map(Object::toString).collect(Collectors.toList()));
    }

}
