import com.clou.uhf.G3Lib.CLReader;

import java.util.HashMap;

/**
 * Created by Алекс on 26/04/2019.
 */

public class ThreadTalk implements Runnable {

    private int PowerTh;
    private String tcpParamTh;

    public ThreadTalk(int power, String tcpParam) {
        // store parameter for later user
        this.PowerTh = power;
        this.tcpParamTh = tcpParam;
    }

    @Override
    public void run() {
        HashMap<Integer, Integer> hashMap = new HashMap<>();

        hashMap.put(1, PowerTh);

        for (HashMap.Entry entry : hashMap.entrySet()) {
            System.out.println("Key: " + entry.getKey() + " Value: "
                    + entry.getValue());
        }
        System.out.println("Sending to " + tcpParamTh);
        CLReader._Config.SetANTPowerParam(tcpParamTh, hashMap);
    }
}
