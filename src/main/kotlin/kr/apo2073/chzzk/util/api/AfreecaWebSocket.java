package kr.apo2073.chzzk.util.api;

import kr.apo2073.chzzk.Chk;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AfreecaWebSocket extends WebSocketClient {
    private final Chk chk=Chk.Companion.getInstance();

    private final AfreecaTVLiveInfo liveInfo;
    private final Map<String, String> afreecaTVUser;
    private final Map<Integer, List<String>> donationRewards;

    private Thread pingThread;

    private boolean isAlive = true;

    private static final String F = "\u000c";
    private static final String ESC = "\u001b\t";

    private static final String COMMAND_PING = "0000";
    private static final String COMMAND_CONNECT = "0001";
    private static final String COMMAND_JOIN = "0002";
    private static final String COMMAND_ENTER = "0004";
    private static final String COMMAND_ENTER_FAN = "0127"; // 0004직후 호출 됨, 입장한 유저의 열혈팬/팬 구분으로 추정
    private static final String COMMAND_CHAT = "0005";
    private static final String COMMAND_DONE = "0018";
    private static final String COMMNAD_C = "0110";
    private static final String COMMNAD_D = "0054";
    private static final String COMMNAD_E = "0090";
    private static final String COMMNAD_F = "0094";

    // 최초 연결시 전달하는 패킷, CONNECT_PACKET = f'{ESC}000100000600{F*3}16{F}'
    private static final String CONNECT_PACKET = makePacket(COMMAND_CONNECT, String.format("%s16%s", F.repeat(3), F));
    // CONNECT_PACKET 전송시 수신 하는 패킷, CONNECT_PACKET = f'{ESC}000100000700{F*3}16|0{F}'
    private static final String CONNECT_RES_PACKET = makePacket(COMMAND_CONNECT, String.format("%s16|0%s", F.repeat(2), F));
    // 주기적으로 핑을 보내서 메세지를 계속 수신하는 패킷, PING_PACKET = f'{ESC}000000000100{F}'
    private static final String PING_PACKET = makePacket(COMMAND_PING, F);

    private Map<String, AfreecaPacket> packetMap = new HashMap<>();
    public AfreecaWebSocket(String serverUri, Draft_6455 draft6455, AfreecaTVLiveInfo liveInfo, Map<String, String> afreecaTVUser, HashMap<Integer, List<String>> donationRewards) {
        super(URI.create(serverUri), draft6455);
        this.setConnectionLostTimeout(0);
        this.setSocketFactory(SSLUtils.createSSLSocketFactory());

        this.liveInfo = liveInfo;
        this.afreecaTVUser = afreecaTVUser;
        this.donationRewards = donationRewards;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Logger.info(ChatColor.GREEN + "[AfreecaTVWebsocket][" + afreecaTVUser.get("nickname") + "] 아프리카 웹소켓 연결이 연결되었습니다.");

        isAlive = true;

        pingThread = new Thread(() -> {
            // Connect msg Send
            byte[] connectPacketBytes = CONNECT_PACKET.getBytes(StandardCharsets.UTF_8);
            send(connectPacketBytes);
            while (isAlive) {
                try {
                    Thread.sleep(59996);

                    byte[] pingPacketBytes = PING_PACKET.getBytes(StandardCharsets.UTF_8);
                    send(pingPacketBytes);

                    for (Map.Entry<String, AfreecaPacket> entry : packetMap.entrySet()) {
                        AfreecaPacket packet = entry.getValue();
                        if (packet.getReceivedTime().isBefore(LocalDateTime.now().minusMinutes(1))) {
                            packetMap.remove(entry.getKey());
                        }
                    }
                } catch (InterruptedException ignore) {
                    Logger.info(ChatColor.RED + "아프리카 웹소켓 핑 스레드가 종료되었습니다.");
                }
            }
        });
        pingThread.start();
    }

    @Override
    public void onMessage(String s) {
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        if (chk.getDebug()) {
            Logger.info(ChatColor.WHITE + "[AfreecaTVWebSocket]["
                    + afreecaTVUser.get("nickname") + "] onMessage: "
                    + new String(bytes.array(), StandardCharsets.UTF_8));
        }

        String message = new String(bytes.array(), StandardCharsets.UTF_8);
        if (CONNECT_RES_PACKET.equals(message)) {
            String CHATNO = liveInfo.CHATNO();
            // 메세지를 내려받기 위해 보내는 패킷, JOIN_PACKET = f'{ESC}0002{calculate_byte_size(CHATNO):06}00{F}{CHATNO}{F*5}'
            String JOIN_PACKET = makePacket(COMMAND_JOIN, String.format("%s%s%s", F, CHATNO, F.repeat(5)));
            byte[] joinPacketBytes = JOIN_PACKET.getBytes(StandardCharsets.UTF_8);
            send(joinPacketBytes);
            return;
        }

        try {
            AfreecaPacket packet = new AfreecaPacket(message.replace(ESC, "").split(F));

            String cmd = packet.getCommand();
            List<String> dataList = switch (cmd) {
                case COMMAND_ENTER -> null;
                case COMMAND_ENTER_FAN -> null;
                default -> packet.getDataList();
            };
            if (dataList == null) {
                return;
            }

            String msg = null;
            String nickname = null;
            int payAmount = 0;
            if (cmd.equals(COMMAND_DONE)) {
                packetMap.put(dataList.get(2), packet);
            } else if (cmd.equals(COMMAND_CHAT)) {
                String nick = dataList.get(5);
                if (packetMap.containsKey(nick)) {
                    AfreecaPacket donePacket = packetMap.get(nick);
                    packetMap.remove(nick);
                    msg = dataList.get(0);
                    nickname = donePacket.getDataList().get(2);
                    payAmount = Integer.parseInt(donePacket.getDataList().get(3)) * 100;
                } else {
                    return;
                }
            } else {
                return;
            }

            if (msg == null || nickname == null || payAmount == 0) {
                return;
            }

            Logger.info(ChatColor.YELLOW + nickname
                    + ChatColor.WHITE + "님께서 " + ChatColor.GREEN
                    + payAmount + "원" + ChatColor.WHITE + "을 후원해주셨습니다.");

            List<String> commands = null;
            if (donationRewards.containsKey(payAmount)) {
                commands = donationRewards.get(payAmount);
            } else {
                commands = donationRewards.get(0);
            }

            if (commands == null) {
                return;
            }

            if (chk.getRandom()) {
                Random rand = new Random();
                int randomIndex = rand.nextInt(commands.size());
                String command = commands.get(randomIndex);
                call(afreecaTVUser.get("tag"), nickname, payAmount, msg, command);
            } else {
                for (String command : commands) {
                    call(afreecaTVUser.get("tag"), nickname, payAmount, msg, command);
                }
            }

        } catch (Exception e) {
            Logger.info(ChatColor.RED + "[AfreecaTVWebsocket]["
                    + afreecaTVUser.get("nickname") + "] 아프리카 메시지 파싱 중 오류가 발생했습니다.");
            Logger.info(ChatColor.LIGHT_PURPLE + e.getMessage());
        }
    }

    private void call(String tag, String nickname, int payAmount, String msg, String command) {
        String [] commandArray = command.split(";");
        for (String cmd : commandArray) {
            String tempCommand = cmd;
            tempCommand = tempCommand.replaceAll("%tag%", tag);
            tempCommand = tempCommand.replaceAll("%name%", nickname);
            tempCommand = tempCommand.replaceAll("%amount%", String.valueOf(payAmount));
            tempCommand = tempCommand.replaceAll("%message%", msg);
            String finalCommand = tempCommand;
            try {
                Bukkit.getScheduler()
                        .callSyncMethod(Objects.requireNonNull(Chk.Companion.getInstance()), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)).get();
            } catch (InterruptedException | ExecutionException e) {
                Logger.info(ChatColor.RED + e.getMessage());
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        Logger.info(ChatColor.RED +
                "[AfreecaTVWebsocket][" + afreecaTVUser.get("nickname") + "] 아프리카 웹소켓 연결이 끊겼습니다.");

        isAlive = false;

        pingThread.interrupt();
        pingThread = null;
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }

    private static String makePacket(String command, String data) {
        return String.format("%s%s%s%s", ESC, command, makeLengthPacket(data), data);
    }

    private static String makeLengthPacket(String data) {
        return String.format("%06d00", data.length());
    }
}

