package com.brotherc.documentcenter.helper;

import com.brotherc.documentcenter.constants.ApiInfoCategoryConstant;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoSaveDTO;
import com.brotherc.documentcenter.model.entity.ApiInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiInfoHelper {

    @Autowired
    private ObjectMapper objectMapper;

    public ApiInfo generateApiInfo(ApiInfo apiInfo, ApiInfoSaveDTO apiInfoSaveDTO) {
        if (apiInfo == null) {
            apiInfo = new ApiInfo();
        }
        BeanUtils.copyProperties(apiInfoSaveDTO, apiInfo);

        String path = buildFullPath(apiInfoSaveDTO.getReqContextPath(), apiInfoSaveDTO.getReqPath());
        path = path.replace("/", "_");
        if (path.startsWith("_")) {
            path = path.substring(1);
        }
        String code = String.join("_", path, apiInfoSaveDTO.getReqMethod().toLowerCase());
        apiInfo.setCode(code);

        ArrayNode reqParam = objectMapper.createArrayNode();
        ArrayNode reqParamDisplay = objectMapper.createArrayNode();

        if (apiInfoSaveDTO.getPathParam() != null) {
            ArrayNode pathParam = apiInfoSaveDTO.getPathParam();
            for (JsonNode element : pathParam) {
                ((ObjectNode) element).put("in", "path");
            }

            ArrayNode schemaNode = pathParam.deepCopy();
            handleParam(schemaNode, ApiInfoCategoryConstant.SCHEMA);
            reqParam.addAll(schemaNode);

            ArrayNode displayNode = pathParam.deepCopy();
            handleParam(displayNode, ApiInfoCategoryConstant.DISPLAY);
            reqParamDisplay.addAll(displayNode);
        }

        if (apiInfoSaveDTO.getQueryParam() != null) {
            ArrayNode queryParam = apiInfoSaveDTO.getQueryParam();
            for (JsonNode element : queryParam) {
                ((ObjectNode) element).put("in", "query");
            }

            ArrayNode schemaNode = queryParam.deepCopy();
            handleParam(schemaNode, ApiInfoCategoryConstant.SCHEMA);
            reqParam.addAll(schemaNode);

            ArrayNode displayNode = queryParam.deepCopy();
            handleParam(displayNode, ApiInfoCategoryConstant.DISPLAY);
            reqParamDisplay.addAll(displayNode);
        }

        if (apiInfoSaveDTO.getRequestBody() != null) {
            JsonNode bodyParam = apiInfoSaveDTO.getRequestBody();
            ((ObjectNode) bodyParam).put("in", "body");

            JsonNode schemaNode = bodyParam.deepCopy();
            handleParam((ObjectNode) schemaNode, ApiInfoCategoryConstant.SCHEMA);
            reqParam.add(schemaNode);

            JsonNode displayNode = bodyParam.deepCopy();
            handleParam((ObjectNode) displayNode, ApiInfoCategoryConstant.DISPLAY);
            reqParamDisplay.add(displayNode);
        }

        JsonNode returnInfo = objectMapper.createObjectNode();
        ArrayNode returnInfoDisplay = objectMapper.createArrayNode();

        if (apiInfoSaveDTO.getResponseBody() != null) {
            JsonNode responseParam = apiInfoSaveDTO.getResponseBody();

            JsonNode schemaNode = responseParam.deepCopy();
            handleParam((ObjectNode) schemaNode, ApiInfoCategoryConstant.SCHEMA);
            returnInfo = schemaNode;

            JsonNode displayNode = responseParam.deepCopy();
            handleParam((ObjectNode) displayNode, ApiInfoCategoryConstant.DISPLAY);
            returnInfoDisplay.add(displayNode);
        }

        try {
            apiInfo.setReqParam(objectMapper.writeValueAsString(reqParam));
            apiInfo.setReqParamDisplay(objectMapper.writeValueAsString(reqParamDisplay));

            apiInfo.setReturnInfo(objectMapper.writeValueAsString(returnInfo));
            apiInfo.setReturnInfoDisplay(objectMapper.writeValueAsString(returnInfoDisplay));
        } catch (JsonProcessingException e) {
            log.error("json转换异常", e);
        }

        return apiInfo;
    }

    private String buildFullPath(String reqContextPath, String reqPath) {
        if (reqPath == null || reqPath.trim().isEmpty()) {
            throw new IllegalArgumentException("reqPath must not be null or empty");
        }

        // 处理 contextPath（可为空）
        if (reqContextPath == null || reqContextPath.trim().isEmpty()) {
            return normalizePath(reqPath);
        }

        return normalizePath(reqContextPath) + "/" + normalizePath(reqPath);
    }

    private String normalizePath(String path) {
        if (path == null) return "";
        return path.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private void handleParam(ArrayNode param, String type) {
        for (JsonNode element : param) {
            handleParam((ObjectNode) element, type);
        }
    }

    private void handleParam(ObjectNode param, String type) {
        String paramType = param.get("type").asText();
        if (ApiInfoCategoryConstant.DISPLAY.equals(type)) {
            if ("array".equals(paramType)) {
                String itemType = param.get(ApiInfoCategoryConstant.CHILDREN).get(0).get("type").asText();
                param.put("type", "array[" + itemType + "]");
                if ("object".equals(itemType) || "array".equals(itemType) || "refObject".equals(itemType)) {
                    JsonNode children = param.get(ApiInfoCategoryConstant.CHILDREN).get(0).get(ApiInfoCategoryConstant.CHILDREN);
                    param.set(ApiInfoCategoryConstant.CHILDREN, children);

                    for (JsonNode element : children) {
                        handleParam((ObjectNode) element, type);
                    }
                } else {
                    param.remove(ApiInfoCategoryConstant.CHILDREN);
                }
            } else if ("object".equals(paramType) || "refObject".equals(paramType)) {
                for (JsonNode element : param.get(ApiInfoCategoryConstant.CHILDREN)) {
                    handleParam((ObjectNode) element, type);
                }
            } else {
                param.remove(ApiInfoCategoryConstant.CHILDREN);
            }
        } else {
            if ("array".equals(paramType)) {
                JsonNode children = param.get(ApiInfoCategoryConstant.CHILDREN);
                param.remove(ApiInfoCategoryConstant.CHILDREN);
                param.set("items", children);
                ((ObjectNode) (children.get(0))).put("name", "");

                handleParam((ObjectNode) (children.get(0)), type);
            } else if ("object".equals(paramType) || "refObject".equals(paramType)) {
                JsonNode children = param.get(ApiInfoCategoryConstant.CHILDREN);
                param.remove(ApiInfoCategoryConstant.CHILDREN);
                param.set("properties", children);

                for (JsonNode element : children) {
                    handleParam((ObjectNode) element, type);
                }
            } else {
                param.remove(ApiInfoCategoryConstant.CHILDREN);
            }
        }
    }

}
