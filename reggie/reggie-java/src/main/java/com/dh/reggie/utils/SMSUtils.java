package com.dh.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

/**
 * 短信发送工具类
 */
public class SMSUtils {

    /**
     * 发送短信
     *
     * @param phoneNumbers 手机号
     * @param param        参数
     */

    public static void sendMessage(String phoneNumbers, String param) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tDHSdQRM56pkbP9bkrq", "PcdYbePHyjDZAA7cjQyR5dNtsOrBAD");

        IAcsClient client = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setSysRegionId("cn-hangzhou");
        request.setPhoneNumbers(phoneNumbers);//接收短信的手机号码
        request.setSignName("南交外卖");//短信签名名称
        request.setTemplateCode("SMS_276490912");//短信模板CODE
        request.setTemplateParam("{\"code\":\""+param+"\"}");//短信模板变量对应的实际值

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
            System.out.println("短信发送成功");
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }
    }

}
