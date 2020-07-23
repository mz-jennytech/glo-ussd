/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ussd.etranzact.glo.web;

import com.etz.ussd.dto.Data;
import com.etz.ussd.session.USSDSession;
import com.etz.ussd.session.USSDSessionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import ussd.etranzact.glo.handler.AirtimeDirectHandler;
import ussd.etranzact.glo.handler.AirtimeDirectThirdPartyHandler;
import ussd.etranzact.glo.handler.AirtimeSelfHandler;
import ussd.etranzact.glo.handler.AirtimeThirdPartyHandler;
import ussd.etranzact.glo.handler.DataDirectSelfHandler;
import ussd.etranzact.glo.handler.DataDirectThirdPartyHandler;
import ussd.etranzact.glo.handler.DataSelfHandler;
import ussd.etranzact.glo.handler.DataThirdPartyHandler;
import ussd.etranzact.glo.service.GloService;

/**
 *
 * @author Damilola.Omowaye
 */
@WebServlet(name = "GloHandler", urlPatterns = {"/webhandler"})
public class GloUSSDWeb extends HttpServlet {

    AirtimeSelfHandler airtimeSelfHandler = new AirtimeSelfHandler();
    AirtimeThirdPartyHandler airtimeThirdPartyHandler = new AirtimeThirdPartyHandler();
    DataSelfHandler dataSelfHandler = new DataSelfHandler();
    DataThirdPartyHandler dataThirdPartyHandler = new DataThirdPartyHandler();

    AirtimeDirectHandler airtimeDirectHandler = new AirtimeDirectHandler();
    AirtimeDirectThirdPartyHandler airtimeDirectThirdPartyHandler = new AirtimeDirectThirdPartyHandler();
    DataDirectSelfHandler dataDirectSelfHandler = new DataDirectSelfHandler();
    DataDirectThirdPartyHandler dataDirectThirdPartyHandler = new DataDirectThirdPartyHandler();

    GloService service = GloService.getInstance();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Logger LOG = Logger.getLogger(GloUSSDWeb.class);
        String process = "";
        USSDSessionManager instance = USSDSessionManager.getInstance();
        Data data = new Data();
        try {

            String sessionId = request.getParameter("sessionid");
            USSDSession session = instance.getSession(sessionId);

            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String nextElement = params.nextElement();
                String parameter = request.getParameter(nextElement);
                data = Data.fillDto(data, nextElement, parameter);
            }

            String message = data.getMessage();
            //boolean isUserExist = false;

            if (session == null) {
                LOG.info(data.getSessionId() + " baseCode >>:::::::::::::::: " + message);
                String[] msgLength = data.getMessage().split("\\*");
                int codeLen = msgLength.length;
                if (message.startsWith("*389*")) {
                    switch (codeLen) {
                        case 5:
                            process = airtimeSelfHandler.process(data); // *389*805*1500*805#
                            break;

                        case 6:
                            String[] msgLen = message.split("\\*");
                            LOG.info(data.getSessionId() + " msgLen[2] >>::::::::::::::::: " + msgLen[2]);
                            switch (msgLen[3]) {
                                case "1":
                                    process = dataSelfHandler.process(data); //*389*805*1*1500*805#
                                    break;

                                default:
                                    process = airtimeThirdPartyHandler.process(data); //*389*805*1500*2348045567890*805#
                                    break;
                            }
                            break;

                        case 7:
                            process = dataThirdPartyHandler.process(data); //*389*805*1*1500*2348045567890*805#
                            break;
                        default:
                            break;
                    }
                } else if (message.startsWith("*805*") || message.startsWith("*777*")) {
                    switch (codeLen) {
                        case 3:
                            process = airtimeDirectHandler.process(data); // *805*<AMOUNT>#                            
                            break;

                        case 4:
                            String[] msgLen1 = message.split("\\*");
                            LOG.info(data.getSessionId() + " msgLen1[2] >>::::::::::::::::: " + msgLen1[2]);
                            switch (msgLen1[2]) {
                                case "1":
                                    process = dataDirectSelfHandler.process(data); //*805*1*<AMOUNT>#
                                    break;
                                default:
                                    process = airtimeDirectThirdPartyHandler.process(data); //*805*<AMOUNT>*msisdn#                                  
                                    break;
                            }
                            break;

                        case 5:
                            String[] msgLen2 = message.split("\\*");
                            LOG.info(data.getSessionId() + " msgLen2[2] >>::::::::::::::::: " + msgLen2[2]);
                            switch (msgLen2[2]) {
                                case "1":
                                    process = dataDirectThirdPartyHandler.process(data); //*805*1*<AMOUNT>*msisdn#                                
                                    break;

                                default:
                                    break;
                            }
                            break;

                        default:
                            process = "Invalid or wrong code dialled.Please dial the correct code. Thank you.";
                            out.println(process);
                            instance.destroySession(sessionId);
                            break;
                    }

                } else {
                    instance.destroySession(sessionId);
                    process = "Invalid or wrong code. Please dial the correct code. Thank you.";
                }

            } else if (session.isSessionIsAlive()) {
                String activeMessage = session.getQueue().get(0);
                String[] msgLength = activeMessage.split("\\*");
                  int codeLen = msgLength.length;
                if (activeMessage.startsWith("*389*")) {
                    switch (codeLen) {
                        case 5:
                            process = airtimeSelfHandler.process(data); // *389*805*1500*805#
                            break;

                        case 6:
                            String[] msgLen = session.getQueue().get(0).split("\\*");
                            LOG.info(data.getSessionId() + " msgLen[2] >>::::::::::::::::: " + msgLen[2]);
                            switch (msgLen[3]) {
                                case "1":
                                    process = dataSelfHandler.process(data); //*389*805*1*1500*805#
                                    break;

                                default:
                                    process = airtimeThirdPartyHandler.process(data); //*389*805*1500*2348045567890*805#
                                    break;
                            }
                            break;

                        case 7:
                            process = dataThirdPartyHandler.process(data); //*389*805*1*1500*2348045567890*805#
                            break;
                        default:
                            break;
                    }
                } else if (activeMessage.startsWith("*805*") || message.startsWith("*777*")) {
                    switch (codeLen) {
                        case 3:
                            process = airtimeDirectHandler.process(data); // *805*<AMOUNT>#                            
                            break;

                        case 4:
                            String[] msgLen1 = session.getQueue().get(0).split("\\*");
                            LOG.info(data.getSessionId() + " msgLen1[2] >>::::::::::::::::: " + msgLen1[2]);
                            switch (msgLen1[2]) {
                                case "1":
                                    process = dataDirectSelfHandler.process(data); //*805*1*<AMOUNT>#
                                    break;
                                default:
                                    process = airtimeDirectThirdPartyHandler.process(data); //*805*<AMOUNT>*msisdn#                                  
                                    break;
                            }
                            break;

                        case 5:
                            String[] msgLen2 = session.getQueue().get(0).split("\\*");
                            LOG.info(data.getSessionId() + " msgLen2[2] >>::::::::::::::::: " + msgLen2[2]);
                            switch (msgLen2[2]) {
                                case "1":
                                    process = dataDirectThirdPartyHandler.process(data); //*805*1*<AMOUNT>*msisdn#                                
                                    break;

                                default:
                                    break;
                            }
                            break;

                        default:
                            process = "Invalid or wrong code dialled.Please dial the correct code. Thank you.";
                            out.println(process);
                            instance.destroySession(sessionId);
                            break;
                    }

                } else {
                    instance.destroySession(sessionId);
                    process = "Invalid or wrong code. Please dial the correct code. Thank you.";
                }

            } else {
                instance.destroySession(sessionId);
                process = "Your session has expired. Please start the process again. Thank you.";
            }

            //This is added to fix the issue of "No Response"
            if (process.equals("")) {
                //@TODO
                //@Important
                //Send an email with the session details to me. This way I will be fixing as it happens.
                //Send email in task. So it does not block
                process = "Invalid entry or system error. Try again later. Thank you.";
                out.println(process);
                instance.destroySession(sessionId);
            } else {
                out.println(process);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
