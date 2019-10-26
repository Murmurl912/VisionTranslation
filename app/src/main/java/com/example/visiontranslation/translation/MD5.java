package com.example.visiontranslation.translation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5缂傛牜鐖滈惄绋垮彠閻ㄥ嫮琚�
 *

 *
 */
public class MD5 {
    // 妫ｆ牕鍘涢崚婵嗩潗閸栨牔绔存稉顏勭摟缁楋附鏆熺紒鍕剁礉閻€劍娼电�涙ɑ鏂佸В蹇庨嚋16鏉╂稑鍩楃�涙顑�
    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /**
     * 閼惧嘲绶辨稉锟芥稉顏勭摟缁楋缚瑕嗛惃鍑狣5閸婏拷
     *
     * @param input 鏉堟挸鍙嗛惃鍕摟缁楋缚瑕�
     * @return 鏉堟挸鍙嗙�涙顑佹稉鑼畱MD5閸婏拷
     *
     */
    public static String md5(String input) {
        if (input == null)
            return null;

        try {
            // 閹峰灝鍩屾稉锟芥稉鐙笵5鏉烆剚宕查崳顭掔礄婵″倹鐏夐幆瀹狀洣SHA1閸欏倹鏆熼幑銏″灇閳ユ紘HA1閳ユ繐绱�
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 鏉堟挸鍙嗛惃鍕摟缁楋缚瑕嗘潪顒佸床閹存劕鐡ч懞鍌涙殶缂侊拷
            byte[] inputByteArray = null;
            try {
                inputByteArray = input.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // inputByteArray閺勵垵绶崗銉ョ摟缁楋缚瑕嗘潪顒佸床瀵版鍩岄惃鍕摟閼哄倹鏆熺紒锟�
            messageDigest.update(inputByteArray);
            // 鏉烆剚宕查獮鎯扮箲閸ョ偟绮ㄩ弸婊愮礉娑旂喐妲哥�涙濡弫鎵矋閿涘苯瀵橀崥锟�16娑擃亜鍘撶槐锟�
            byte[] resultByteArray = messageDigest.digest();
            // 鐎涙顑侀弫鎵矋鏉烆剚宕查幋鎰摟缁楋缚瑕嗘潻鏂挎礀
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 閼惧嘲褰囬弬鍥︽閻ㄥ嚜D5閸婏拷
     *
     * @param file
     * @return
     */
    public static String md5(File file) {
        try {
            if (!file.isFile()) {
                System.err.println("閺傚洣娆�" + file.getAbsolutePath() + "娑撳秴鐡ㄩ崷銊﹀灗閼板懍绗夐弰顖涙瀮娴狅拷");
                return null;
            }

            FileInputStream in = new FileInputStream(file);

            String result = md5(in);

            in.close();

            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String md5(InputStream in) {

        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                messagedigest.update(buffer, 0, read);
            }

            in.close();

            String result = byteArrayToHex(messagedigest.digest());

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String byteArrayToHex(byte[] byteArray) {
        // new娑擄拷娑擃亜鐡х粭锔芥殶缂佸嫸绱濇潻娆庨嚋鐏忚鲸妲搁悽銊︽降缂佸嫭鍨氱紒鎾寸亯鐎涙顑佹稉鑼畱閿涘牐袙闁插﹣绔存稉瀣剁窗娑擄拷娑撶寵yte閺勵垰鍙撴担宥勭癌鏉╂稑鍩楅敍灞肩瘍鐏忚鲸妲�2娴ｅ秴宕勯崗顓＄箻閸掕泛鐡х粭锔肩礄2閻拷8濞嗏剝鏌熺粵澶夌艾16閻拷2濞嗏剝鏌熼敍澶涚礆
        char[] resultCharArray = new char[byteArray.length * 2];
        // 闁秴宸荤�涙濡弫鎵矋閿涘矂锟芥俺绻冩担宥堢箥缁犳绱欐担宥堢箥缁犳鏅ラ悳鍥彯閿涘绱濇潪顒佸床閹存劕鐡х粭锔芥杹閸掓澘鐡х粭锔芥殶缂佸嫪鑵戦崢锟�
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        // 鐎涙顑侀弫鎵矋缂佸嫬鎮庨幋鎰摟缁楋缚瑕嗘潻鏂挎礀
        return new String(resultCharArray);

    }

}
