package cn.itcast.core.listener;

import cn.itcast.core.service.PageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

public class PageListener implements MessageListener {

    @Autowired
    private PageService pageService;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage amt = (ActiveMQTextMessage)message;
        try {
            //获取文本消息，商品id
            String id = amt.getText();
            long goodsId = Long.parseLong(id);
            Map<String, Object> rootMap = pageService.findGoodsData(goodsId);
            pageService.createStaticPage(goodsId,rootMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
