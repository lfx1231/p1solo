package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Value("${template_code}")
    private String templateCode;
    @Value("${sign_name}")
    private String signName;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQQueue smsDestination;
    @Autowired
    private UserDao userDao;

    @Override
    public void sendCode(final String phone) {

        //随机数，生成6位数的验证码。。
        Random random = new Random();
       final long code =(long) random.nextInt(900001)+100000;

        redisTemplate.boundValueOps(phone).set(code,60, TimeUnit.MINUTES);
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMsg = session.createMapMessage();
                mapMsg.setString("templateCode",templateCode);
                mapMsg.setString("signName",signName);
                mapMsg.setString("phone",phone);
                Map<String,String> contentMap = new HashMap<>();
                contentMap.put("code",String.valueOf(code));
                mapMsg.setString("content", JSON.toJSONString(contentMap));
                return mapMsg;
            }
        });
    }

    @Override
    public void add(User user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());
        user.setStatus("Y");
        userDao.insertSelective(user);
    }

    @Override
    public Boolean checkCode(String phone, String smscode) {
        if(phone==null||"".equals(phone) ||smscode == null ||"".equals(smscode) ){
            return false;
        }
        Long sendCode =(Long) redisTemplate.boundValueOps(phone).get();
        if(sendCode==null || "".equals(sendCode)){
            return false;
        }
        if(smscode.equals(String.valueOf(sendCode))){
            //验证码验证成功后，删除redis中保存的验证码
          //  redisTemplate.delete(phone);
            return true;
        }
        return false;
    }

}
