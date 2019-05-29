package cn.itcast.core.service;

public interface SolrService {
    void addItemToSolr(Long[] ids);

    void deleteSolrByid(Long[] ids);
}
