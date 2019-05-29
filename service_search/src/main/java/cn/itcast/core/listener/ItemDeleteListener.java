package cn.itcast.core.listener;

import cn.itcast.core.service.SolrService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

//自定义 监听器
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrService solrService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage amt = (ActiveMQTextMessage)message;
        try {
            String id = amt.getText();
            long goodsId = Long.parseLong(id);
            Long[] arr = {goodsId};
            solrService.deleteSolrByid(arr);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
