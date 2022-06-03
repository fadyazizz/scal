package Netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.*;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = Logger.getLogger(HttpUploadServerHandler.class.getName());

    private HttpRequest request;

    private HttpData partialContent;

    private final StringBuilder responseContent = new StringBuilder();

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed

    private HttpPostRequestDecoder decoder;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            URI uri = new URI(request.uri());
           
            

            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                e1.printStackTrace();
                //responseContent.append(e1.getMessage());
               // writeResponse(ctx.channel(), true);
                return;
            }

            boolean readingChunks = HttpUtil.isTransferEncodingChunked(request);
            
            
        }

        // check if the decoder was constructed before
        // if not it handles the form get
        if (decoder != null) {
            if (msg instanceof HttpContent) {
                // New chunk is received
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                    e1.printStackTrace();
//                   
                    return;
                }
                
                readHttpDataChunkByChunk();
                // example of reading only if at the end
                if (chunk instanceof LastHttpContent) {
                    System.out.println("here1231231231");
                    //writeResponse(ctx.channel());

                    reset();
                }
            }
        } else {
            //writeResponse(ctx.channel());
        }
    }

    private void reset() {
        request = null;

        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }

    /**
     * Example of reading request by chunk and getting values from chunk to chunk
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously set as partial
                    if (partialContent == data) {
                        logger.info(" 100% (FinalSize: " + partialContent.length() + ")");
                        partialContent = null;
                    }
                    // new value
                   
                }
            }
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ")
                                .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        builder.append("Start Attribute: ")
                                .append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    logger.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    logger.info(builder.toString());
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
            responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    

   
   
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(Level.WARNING, responseContent.toString(), cause);
        ctx.channel().close();
    }
}