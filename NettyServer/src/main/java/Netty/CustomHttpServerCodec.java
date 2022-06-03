package Netty;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Forked from HttpServerCodec. This disables chunked-encoding support since it isn't necessary for Http Streaming
 * Server.
 *
 */
public final class CustomHttpServerCodec
        extends CombinedChannelDuplexHandler<CustomHttpRequestDecoder, HttpResponseEncoder> {

    /**
     * Creates a new instance with the default decoder options ({@code maxInitialLineLength (4096}},
     * {@code maxHeaderSize (8192)}, and {@code maxChunkSize (8192)}).
     */
    public CustomHttpServerCodec() {
        this(4096000, 8192000, 8192000);
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public CustomHttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(new CustomHttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize),
                new HttpResponseEncoder());
    }

    /**
     * Creates a new instance with the specified decoder options.
     */
    public CustomHttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize,
                                 boolean validateHeaders) {
        super(new CustomHttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders),
                new HttpResponseEncoder());
    }
}