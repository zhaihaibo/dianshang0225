package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.user.bean.PmsSearchParam;
import com.atguigu.gmall.user.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.user.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FieldMaskingSpanQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {
        String searchByQueryStr = getSearchByQueryStr(pmsSearchParam);
        System.err.println(searchByQueryStr);

        Search search = new Search.Builder(searchByQueryStr).addIndex("gmall0225").addType("PmsSearchSkuInfo").build();
        //执行查询
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(searchResult);
        //从查询结果中得到指定valueId的source内容
        List<PmsSearchSkuInfo> list = new ArrayList<>();
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        if (hits != null && hits.size() > 0) {
            for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
                PmsSearchSkuInfo source = hit.source;
                //解析高亮
                Map<String, List<String>> highlight = hit.highlight;
                if (highlight != null && highlight.size() > 0) {
                    List<String> list1 = highlight.get("skuName");
                    source.setSkuName(list1.get(0));
                }
                list.add(source);
            }
        }
        System.out.println(list.size());

        return list;
    }

    public String getSearchByQueryStr(PmsSearchParam pmsSearchParam) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);
        //search - bool - must -match
        //              - filter - term
        //query下的bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //must下的match
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);

            //bool下的must
            boolQueryBuilder.must(matchQueryBuilder);
        }

        String catalog3Id = pmsSearchParam.getCatalog3Id();
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }


        //filter下的term
        String[] valueId = pmsSearchParam.getValueId();
        if (valueId != null && valueId.length > 0) {
            for (String s : valueId) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", s);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //bool下的filter

        //封装查询操作
        searchSourceBuilder.query(boolQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        highlightBuilder.preTags("<span style='color:green;font-weight:bolder'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");

        searchSourceBuilder.highlight(highlightBuilder);

        return searchSourceBuilder.toString();


    }
}
