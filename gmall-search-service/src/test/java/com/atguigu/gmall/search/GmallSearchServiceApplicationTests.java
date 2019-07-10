package com.atguigu.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.user.bean.PmsSkuInfo;
import com.atguigu.gmall.user.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;

    @Test
    public void testSearch() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);
        //search - bool - must -match
        //              - filter - term
        //query下的bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //must下的match
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "苹果");
        //bool下的must
        boolQueryBuilder.must(matchQueryBuilder);

        //filter下的term
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", "107");

        //bool下的filter
        boolQueryBuilder.filter(termQueryBuilder);

        //封装查询操作
        searchSourceBuilder.query(boolQueryBuilder);
        System.err.println(searchSourceBuilder.toString());
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex("gmall0225").addType("PmsSearchSkuInfo").build();
        //执行查询
        SearchResult searchResult = jestClient.execute(search);
        System.out.println(searchResult);
        //从查询结果中得到指定valueId的source内容
        List<PmsSearchSkuInfo> list = new ArrayList<>();
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = searchResult.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            list.add(source);
        }
        System.out.println(list.size());

    }

    @Test
    public void testIndex() throws IOException {

        List<PmsSkuInfo> skuInfos = skuService.getAllSku();
        List<PmsSearchSkuInfo> list = new ArrayList<>();
        for (PmsSkuInfo skuInfo : skuInfos) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(skuInfo, pmsSearchSkuInfo);
            String id = skuInfo.getId();
            long l = Long.parseLong(id);
            pmsSearchSkuInfo.setId(l);
            list.add(pmsSearchSkuInfo);
        }


        for (PmsSearchSkuInfo pmsSearchSkuInfo : list) {
            Index index = new Index.Builder(pmsSearchSkuInfo).index("gmall0225").type("PmsSearchSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();

            JestResult execute = jestClient.execute(index);
        }


    }

}
