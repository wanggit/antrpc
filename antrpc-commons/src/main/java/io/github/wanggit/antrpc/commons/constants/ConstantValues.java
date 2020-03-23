package io.github.wanggit.antrpc.commons.constants;

public interface ConstantValues {

    /** 心跳包类型 */
    byte HB_TYPE = 0x1;

    /** 业务包类型 */
    byte BIZ_TYPE = 0x2;

    /** 是否压缩 */
    byte COMPRESSED = 0x3;

    /** 是否压缩 */
    byte UNCOMPRESSED = 0x4;

    /** 是否已加密 */
    byte CODECED = 0x5;

    /** 是否已加密 */
    byte UNCODCED = 0x6;

    /** 是否需要压缩 */
    int NEED_COMPRESS_LENGTH = 1024;

    int DECODER_HEADER_SIZE = 16;

    /** 发送的数据包最大长度 */
    int DECODER_MAX_FRAME_LENGTH = 1024 * 1024;

    /** 长度域偏移量，指的是长度域位于整个数据包字节数组中的下标。0表示长度在第一个字节 */
    int DECODER_LENGTH_FIELD_OFFSET = 0;

    /** 长度域的自己的字节数长度 */
    int DECODER_LENGTH_FIELD_LENGTH = 4;

    /**
     * 长度域的偏移量矫正。 如果长度域的值，除了包含有效数据域的长度外， 还包含了其他域（如长度域自身）长度， 那么，就需要进行矫正。矫正的值为：包长 - 长度域的值 – 长度域偏移 –
     * 长度域长
     */
    int DECODER_LENGTH_ADJUSTMENT = 0;

    /** 丢弃的起始字节数。丢弃处于有效数据前面的字节数量。比如前面有4个节点的长度域，则它的值为4 */
    int DECODER_INITIAL_BYTES_TO_STRIP = 4;

    /** 注册ZK节点的根命名空间 */
    String ZK_ROOT_NODE_NAME = "__rpc_na__";

    /** 订阅信息的ZK节点根命名空间 */
    String ZK_ROOT_SUBSCRIBE_NODE_NAME = "__rpc_sub__";

    /** rpc的默认端口 */
    int RPC_DEFAULT_PORT = 6060;

    /** zookeeper默认地址 */
    String RPC_DEFAULT_ZK_SERVER = "localhost:2181";

    /** antrpc 配置有前缀 */
    String ANTRPC_CONFIG_PREFIX = "antrpc";

    /** 默认发送日志的Topic */
    String CALL_LOG_KAFKA_TOPIC = "antrpc_call_log";

    /** 服务之间连接的熔断器阀值，表示10秒内出现5次连接断开，就打开熔断器 */
    int CONNECTION_CIRCUIT_BREAKER_THRESHOLD = 8;

    /** 服务之间连接的熔断器状态检查时间周期，表示10秒内出现5次连接断开，就打开熔断器 */
    int CONNECTION_CIRCUIT_BREAKER_CHECK_INTERVAL_SECONDS = 20;

    String BANNER =
            "\n"
                    + "     _          _   ____             \n"
                    + "    / \\   _ __ | |_|  _ \\ _ __   ___ \n"
                    + "   / _ \\ | '_ \\| __| |_) | '_ \\ / __|\n"
                    + "  / ___ \\| | | | |_|  _ <| |_) | (__ \n"
                    + " /_/   \\_\\_| |_|\\__|_| \\_\\ .__/ \\___|\n"
                    + "                         |_|         \n";
}
