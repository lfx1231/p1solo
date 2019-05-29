package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;

    @Override
    public void add(Seller seller) {

       seller.setStatus("0");
       seller.setCreateTime(new Date());
        sellerDao.insertSelective(seller);

    }

    @Override
    public PageResult findPage(Seller seller,Integer page,Integer rows) {
        PageHelper.startPage(page,rows);
        SellerQuery query = new SellerQuery();
        SellerQuery.Criteria criteria = query.createCriteria();
        if(seller!=null){
            if(seller.getStatus()!=null && !"".equals(seller.getStatus())){
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if(seller.getName()!=null && !"".equals(seller.getName())){
                criteria.andNameLike("%"+seller.getName()+"%");
            }
        }
        Page<Seller> sellers =(Page<Seller>) sellerDao.selectByExample(query);
        return new PageResult(sellers.getTotal(),sellers.getResult());
    }
    //根据id查询一个第三方卖家的详细信息。
    @Override
    public Seller findOne(String id) {
        Seller seller = sellerDao.selectByPrimaryKey(id);
        return seller;
    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setStatus(status);
        seller.setSellerId(sellerId);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
