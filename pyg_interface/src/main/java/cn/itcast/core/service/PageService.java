package cn.itcast.core.service;

import java.util.Map;

public interface PageService {

    public Map<String,Object> findGoodsData(Long goodsId);
    public void createStaticPage(Long goodsId,Map<String, Object> rootMap)throws Exception;

}
