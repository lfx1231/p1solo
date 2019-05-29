package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Override
    public PageResult search(Specification spec,Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        SpecificationQuery query = new SpecificationQuery();
        if(spec.getSpecName()!=null &&!"".equals(spec.getSpecName())){
            query.createCriteria().andSpecNameLike("%"+spec.getSpecName()+"%");
        }
        Page<Specification> specList= (Page<Specification>)specDao.selectByExample(query);
        return new PageResult(specList.getTotal(),specList.getResult());
    }

    @Override
    public void add(SpecEntity specEntity) {

            specDao.insertSelective(specEntity.getSpecification());

        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                    option.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(option);
            }
        }
    }

    @Override
    public SpecEntity findOne(Long id) {
        if(id!=null){
            Specification spec = specDao.selectByPrimaryKey(id);
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = query.createCriteria().andSpecIdEqualTo(id);
            List<SpecificationOption> optionList = specificationOptionDao.selectByExample(query);
            return new SpecEntity(spec,optionList);
        }

        return null;

    }

    @Override
    public void update(SpecEntity specEntity) {

        specDao.updateByPrimaryKeySelective(specEntity.getSpecification());

        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(specEntity.getSpecification().getId());
        specificationOptionDao.deleteByExample(query);

        if(specEntity.getSpecificationOptionList()!=null){
            for (SpecificationOption option : specEntity.getSpecificationOptionList()) {
                    option.setSpecId(specEntity.getSpecification().getId());
                specificationOptionDao.insertSelective(option);

            }
        }
    }

    @Override
    public void delete(Long[] ids) {
        if(ids!=null){
            for (Long id : ids) {
                specDao.deleteByPrimaryKey(id);
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(query);
            }
        }
    }

    @Override
    public List<Map> selectOptionList() {
      List<Map> maps =  specDao.selectOptionList();

        return maps;
    }


}
