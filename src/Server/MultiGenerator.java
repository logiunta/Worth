package Server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Random;

public class MultiGenerator {
    private int ip1, ip2, ip3, ip4;
    private String initAddress;
    private String lastReusedIp;
    private final ArrayList<String> reusedIp;

    @JsonCreator
    public MultiGenerator(@JsonProperty("initAddress")String initAddress,@JsonProperty("reusedIp")ArrayList<String> reusedIp) {
        this.initAddress = initAddress;
        this.reusedIp = reusedIp;
        this.lastReusedIp = null;
        String[] ipSplitted = initAddress.split("\\.");
        ip1 = Integer.parseInt(ipSplitted[0]);
        ip2 = Integer.parseInt(ipSplitted[1]);
        ip3 = Integer.parseInt(ipSplitted[2]);
        ip4 = Integer.parseInt(ipSplitted[3]);

    }


    public String generateIp() {
            lastReusedIp = fromReused();
            if (lastReusedIp == null) {
                if (ip4 < 255) {
                    ip4++;
                    initAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                } else if (ip3 < 255) {
                    ip4 = 0;
                    ip3++;
                    initAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;

                } else if (ip2 < 255) {
                    ip3 = 0;
                    ip4 = 0;
                    ip2++;
                    initAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;

                } else if (ip1 < 255) {
                    ip2 = 0;
                    ip3 = 0;
                    ip4 = 0;
                    ip1++;
                    initAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;

                }

            } else {
                reusedIp.remove(lastReusedIp);
                return lastReusedIp;
            }

            return initAddress;

    }


    public String getInitAddress() {
        return initAddress;
    }

    public ArrayList<String> getReusedIp() {
        return reusedIp;
    }


    public void addReusedIp(String ip) {
        if (!reusedIp.contains(ip))
            reusedIp.add(ip);

    }

    private String fromReused() {
        if (reusedIp.isEmpty())
            return null;
        Random rand = new Random();
        int index = rand.nextInt((reusedIp.size()));
        return reusedIp.get(index);
    }




}
