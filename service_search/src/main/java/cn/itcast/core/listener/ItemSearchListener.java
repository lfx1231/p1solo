package cn.itcast.core.listener;

import cn.itcast.core.service.SolrService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

//自定义监听器
public class ItemSearchListener implements MessageListener{

    @Autowired
    private SolrService solrService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage amt = (ActiveMQTextMessage) message;
        try {
            String id = amt.getText();
            long goodsId = Long.parseLong(id);
            Long[] arr = {goodsId};
            solrService.addItemToSolr(arr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
