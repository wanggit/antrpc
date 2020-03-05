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

    /** 注册ZK节点的根命名空间 */
    String ZK_ROOT_NODE_NAME = "__rpc_na__";

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
                    + "                                                                             \n"
                    + "       db                              88888888ba                            \n"
                    + "      d88b                      ,d     88      \"8b                           \n"
                    + "     d8'`8b                     88     88      ,8P                           \n"
                    + "    d8'  `8b      8b,dPPYba,  MM88MMM  88aaaaaa8P'  8b,dPPYba,    ,adPPYba,  \n"
                    + "   d8YaaaaY8b     88P'   `\"8a   88     88\"\"\"\"88'    88P'    \"8a  a8\"     \"\"  \n"
                    + "  d8\"\"\"\"\"\"\"\"8b    88       88   88     88    `8b    88       d8  8b          \n"
                    + " d8'        `8b   88       88   88,    88     `8b   88b,   ,a8\"  \"8a,   ,aa  \n"
                    + "d8'          `8b  88       88   \"Y888  88      `8b  88`YbbdP\"'    `\"Ybbd8\"'  \n"
                    + "                                                    88                       \n"
                    + "                                                    88                       \n";
}
