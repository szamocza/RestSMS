package net.xcreen.restsms;

import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet
@MultipartConfig
public class SMSServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Log.i("SMS-Servlet", "Request /send");
        //Init Gson/PhoneNumberUtil
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        //Set Response
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        String message = request.getParameter("message");
        String phoneno = request.getParameter("phoneno");

        //Check if post-parameters exists
        if(message == null || phoneno == null){
            //Return Failing JSON
            Log.i("SMS-Servlet", "Invalid message or phoneno");
            response.getWriter().println(gson.toJson(new SMSResponse(false, "message or phoneno parameter are missing!")));
            return;
        }

        //Check if message is valid
        if(message.length() < 1 || message.length() > 160){
            Log.i("SMS-Servlet", "Invalid message (message-length: " + message.length() + ")");
            //Return Failing JSON
            response.getWriter().println(gson.toJson(new SMSResponse(false, "message should be between 1 and 160 chars!")));
            return;
        }

        //Check if phoneno is valid and parse it
        Phonenumber.PhoneNumber phoneNumber;
        try {
            phoneNumber = phoneUtil.parse(phoneno, null);
        }
        catch (Exception ex) {
            Log.i("SMS-Servlet", "Failed to parse phoneno");
            ex.printStackTrace();
            //Return Failing JSON
            response.getWriter().println(gson.toJson(new SMSResponse(false, "Invalid phoneno (make sure you include the + with Country-Code)!")));
            return;
        }

        //Send SMS
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL), null, message, null, null);
        //Show Success message
        response.getWriter().println(gson.toJson(new SMSResponse(true, null)));
    }
}
