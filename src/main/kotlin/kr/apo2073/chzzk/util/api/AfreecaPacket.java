package kr.apo2073.chzzk.util.api;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class AfreecaPacket {
    private final String command;
    private final List<String> dataList;

    private final LocalDateTime receivedTime = LocalDateTime.now();

    public AfreecaPacket(String[] args) {
        this.dataList = new ArrayList<>(Arrays.asList(args));
        String cmd = dataList.remove(0);
        this.command = cmd.substring(0, 4);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Command: ").append(command).append("\n");
        sb.append("Data: ");
        for (String d : dataList) {
            sb.append(d).append(" ");
        }
        return sb.toString();
    }
}
