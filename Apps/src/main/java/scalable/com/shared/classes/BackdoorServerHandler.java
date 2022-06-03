package scalable.com.shared.classes;

import Netty.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.FileInputStream;
import java.io.IOException;
 

import io.netty.handler.codec.base64.Base64;

import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import scalable.com.exceptions.ValidationException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


@ChannelHandler.Sharable
public class BackdoorServerHandler extends ChannelInboundHandlerAdapter {
    private final Controller controller;

    String methodType;
    String uri;
    JSONObject body = new JSONObject();
    JSONObject uriParams;
    HttpRequest req;
    JSONObject headers;
    String queueName;
    JSONObject token;
    HttpPostRequestDecoder requestDecoder;
    boolean isFormData;
    String commandName;
    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static boolean isEmptyHttpContent(HttpContent httpContent) {
        return httpContent.equals(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    private void errorResponse(ChannelHandlerContext ctx, int code, String msg) {
        JSONObject response = new JSONObject().put("statusCode", code).put("msg", msg);
        ByteBuf content = Unpooled.copiedBuffer(response.toString(), CharsetUtil.UTF_8);
        ctx.writeAndFlush(content);
    }

 

    private JSONObject getHeaders() {
        headers = new JSONObject();
        req.headers().entries().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return headers;
    }

    private JSONObject getToken() {

        String authHeader = headers.has("Authorization") ? headers.getString("Authorization") : null;
        JSONObject authenticationParams = JWTHandler.getUnauthorizedAuthParams();

        if (authHeader != null) {
            String[] auth = authHeader.split(" ");
            if (auth.length > 1) {
                authenticationParams = JWTHandler.decodeToken(auth[1]);
            }
        }
        JSONObject returnObject=new JSONObject();
        Object tokenPayload=authenticationParams.has(JWTHandler.TOKEN_PAYLOAD)?authenticationParams.get(JWTHandler.TOKEN_PAYLOAD):new JSONObject();
        returnObject.put(JWTHandler.TOKEN_PAYLOAD,tokenPayload);
        returnObject.put(JWTHandler.IS_AUTHENTICATED,authenticationParams.get(JWTHandler.IS_AUTHENTICATED));
        System.out.println(returnObject.toString()+"mkfmd");
        return returnObject ;
    }
    private String getQueueName(){

        String[] queueName=this.uri.split("/");
        return queueName[1];
    }
    private String getCommandName(){

        String[] queueName=this.uri.split("/");
        return queueName[2].split("\\?")[0];
    }
    JSONObject getURIParams() {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        String[] uriPathFields = decoder.path().substring(1).split("/");

        uriParams = new JSONObject();
        Set<Map.Entry<String, List<String>>> uriParamsSet = decoder.parameters().entrySet();
        uriParamsSet.forEach(entry -> uriParams.put(entry.getKey(), entry.getValue().get(0)));
        return uriParams;
    }

    JSONObject packRequest() throws IOException, JSONException {
        JSONObject request = new JSONObject();

        request.put("body", body);
        request.put("uriParams", uriParams);
        request.put("methodType", methodType);
        request.put("headers", headers);
        request.put("commandName",commandName);
        request.put(JWTHandler.IS_AUTHENTICATED,token.get(JWTHandler.IS_AUTHENTICATED));
        request.put(JWTHandler.TOKEN_PAYLOAD,token.get(JWTHandler.TOKEN_PAYLOAD));


        if (requestDecoder != null) {
            System.out.println(6);
            JSONObject httpData = readHttpData();
            System.out.println(7);
            //System.out.println(httpData.getString("email")+"httpdataaaa");
            httpData.keySet().forEach(key -> request.put(key, httpData.getJSONObject(key)));
        }
        System.out.println(request);
        return request;
    }





    @Override
    public String toString() {
        return "UserHandler{" +
                "methodType='" + methodType + '\'' +
                ", uri='" + uri + '\'' +
                ", body=" + body +
                ", uriParams=" + uriParams +
                '}';
    }



    private JSONObject readHttpData() throws IOException, JSONException {
        JSONObject data = new JSONObject();
        JSONObject files = new JSONObject();
        while (requestDecoder.hasNext()) {
            InterfaceHttpData httpData = requestDecoder.next();
            if (httpData.getHttpDataType() == HttpDataType.Attribute) {

                Attribute attribute = (Attribute) httpData;
                System.out.println(attribute.getName()+"readHTTPDATA");
                body.put(attribute.getName(),attribute.getValue());
                //data.put(attribute.getName(), new JSONObject(attribute.getValue()));
            } else if (httpData.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) httpData;
                 byte[] byteData=fileUpload.get();
                 System.out.println(byteData.length+"heandjnabnedjh");
                
                JSONObject jsonFile = new JSONObject();
                String encodedData = Base64.encode(fileUpload.getByteBuf()).toString(StandardCharsets.UTF_8);
                jsonFile.put("data",encodedData);
                jsonFile.put("byteData",byteData);
                jsonFile.put("type", fileUpload.getContentType());
                files.put(fileUpload.getName(), jsonFile);
            }
        }
        return data.put("files", files);
    }

    private void cleanUp() {
        if (requestDecoder != null) {
            requestDecoder.destroy();
            requestDecoder = null;
        }
    }


    public BackdoorServerHandler(Controller controller) {
        this.controller = controller;
    }

    private String toString(Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (buf.isReadable()) {
            sb.append((char) buf.readByte());
        }
        return sb.toString();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("in channel read request handler");
        JSONObject responseObject;
        if (msg instanceof HttpRequest) {
            req = (HttpRequest) msg;
            uri = req.uri();

            methodType = req.method().toString();
            uriParams = getURIParams();
            queueName= getQueueName();

            headers = getHeaders();
            token=getToken();
            commandName=getCommandName();
            //System.out.println("token: " +token+" queueName: "+queueName+" command name: "+commandName);
            ctx.channel().attr(Server.REQ_KEY).set(req);
            if(headers.has("Content-Type")) {
                isFormData = headers.getString("Content-Type").split(";")[0].equals("multipart/form-data");
            }
        }
        if (msg instanceof HttpContent && !isFormData) {
            System.out.println(1);
            HttpContent content = (HttpContent) msg;
//            if (isEmptyHttpContent(content))
//                return;
            ByteBuf jsonBuf = content.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            if (!methodType.equals("GET") && !jsonStr.isEmpty()) {
                try {
                    body = new JSONObject(jsonStr);
                } catch (JSONException e) {
                    errorResponse(ctx, 400, "Incorrect Body");
                    return;
                }
            }
        }

        if (msg instanceof FullHttpRequest) {
            System.out.println(2);
            if (!methodType.equals("GET") && isFormData) {
                System.out.println(3);
                requestDecoder = new HttpPostRequestDecoder((FullHttpRequest) msg);

                requestDecoder.setDiscardThreshold(0);
            }
        }
        if (msg instanceof LastHttpContent) {
            System.out.println(4);
           
                try {
                    System.out.println(5);
                    JSONObject request = packRequest();
                    System.out.println(request);
                   String response=this.controller.handleControllerMessage(request);
                    responseObject=new JSONObject(response);
                    ByteBuf content = Unpooled.copiedBuffer(responseObject.toString(), CharsetUtil.UTF_8);
                    ctx.fireChannelRead(content.copy());
                   //returnDefaultResponse(ctx,new JSONObject(response));
                } catch (IOException | JSONException e) {
                    System.out.println(e.getMessage());
                    String tempVar=Responder.makeErrorResponse("something went wrong",404);
                    responseObject=new JSONObject(tempVar);
                    ByteBuf content = Unpooled.copiedBuffer(responseObject.toString(), CharsetUtil.UTF_8);
                    ctx.fireChannelRead(content.copy());
                     //responseObject=new JSONObject(tempVar);
                    //returnDefaultResponse(ctx,responseObject);
                } catch (ValidationException e) {
                    System.out.println("here");
                    String tempVar=Responder.makeErrorResponse("something went wrong",404);
                    responseObject=new JSONObject(tempVar);
                    ByteBuf content = Unpooled.copiedBuffer(responseObject.toString(), CharsetUtil.UTF_8);
                    ctx.fireChannelRead(content.copy());
                    //responseObject=new JSONObject(tempVar);
                    //returnDefaultResponse(ctx,responseObject);
                    //throw new RuntimeException(e);
                }

        }


    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
