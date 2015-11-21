package com.android.mms.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import com.android.mms.R;
import android.content.Context;


/**
 * Created by Administrator on 2014/12/24 0024.
 */
public class StringUtils implements StaticObjectInterface {

    private StringUtils() {}

    /**
     * 判断字符串中时否包含中文
     *
     * @param str
     * @return
     */
    public static boolean isContainsChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg || str.contains("【") || str.contains("】") || str.contains("。");
    }

    public static boolean isPersonalMoblieNO(String mobiles) {
        if(mobiles != null) {
            Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
            Matcher m = p.matcher(mobiles);
            if (m == null) {
                return false;
            }
            else {
                return m.matches();
            }
        }
        return false;
    }

    public static String tryToGetCaptchas(String str) {
        Pattern continuousNumberPattern = Pattern.compile("[a-zA-Z0-9\\.]+");
        Matcher m = continuousNumberPattern.matcher(str);
        String mostLikelyCaptchas = "";
        int currentLevel = -1; //只有字母相似级别为0， 只有字母和数字可能级别为1, 只有数字可能级别为2.
        while (m.find()) {
            if (m.group().length() > 3 && m.group().length() < 8 && !m.group().contains(".")) {
                if(isNearToKeyWord(m.group(), str)) {
                    final String strr = m.group();
                    if(currentLevel == -1) {
                        mostLikelyCaptchas = m.group();
                    }
                    final int level = getLikelyLevel(m.group());
                    if(level > currentLevel) {
                        mostLikelyCaptchas = m.group();
                    }
                    currentLevel = level;
                }
            }
        }
        return mostLikelyCaptchas;
    }

    public static String tryToGetCaptchasEn(String str) {
        Pattern continuousNumberPattern = Pattern.compile("[0-9\\.]+");
        Matcher m = continuousNumberPattern.matcher(str);
        while (m.find()) {
            if (m.group().length() > 3 && m.group().length() < 8 && !m.group().contains(".")) {
                if(isNearToKeyWordEn(m.group(), str)) {
                    return m.group();
                }
            }
        }
        return "";
    }

    private static  int getLikelyLevel(String str) {
        if(str.matches("^[0-9]*$")) {
            return 2;
        } else if(str.matches("^[a-zA-Z]*$")) {
            return 0;
        } else {
            return 1;
        }

    }

    public static boolean isNearToKeyWordEn(String currentStr, String content) {
        int startPosition = 0;
        int endPosition = content.length() - 1;
        if (content.indexOf(currentStr) > 12) {
            startPosition = content.indexOf(currentStr) - 12;
        }
        if (content.indexOf(currentStr)  + currentStr.length() + 12 < content.length() - 1) {
            endPosition = content.indexOf(currentStr) + currentStr.length() + 12;
        }
        Boolean isNearToKeyWord = false;
        for (int i = 0; i < CPATCHAS_KEYWORD_EN.length; i++) {
            if (content.substring(startPosition, endPosition).contains(CPATCHAS_KEYWORD_EN[i])) {
                isNearToKeyWord = true;
                break;
            }
        }
        return isNearToKeyWord;
    }

    public static boolean isNearToKeyWord(String currentStr, String content) {
        int startPosition = 0;
        int endPosition = content.length() - 1;
        if (content.indexOf(currentStr) > 12) {
            startPosition = content.indexOf(currentStr) - 12;
        }
        if (content.indexOf(currentStr)  + currentStr.length() + 12 < content.length() - 1) {
            endPosition = content.indexOf(currentStr) + currentStr.length() + 12;
        }
        Boolean isNearToKeyWord = false;
        for (int i = 0; i < CPATCHAS_KEYWORD.length; i++) {
            if (content.substring(startPosition, endPosition).contains(CPATCHAS_KEYWORD[i])) {
                isNearToKeyWord = true;
                break;
            }
        }
        return isNearToKeyWord;
    }

    public static boolean isCaptchasMessage(String content) {
        Boolean isCaptchasMessage = false;
        for (int i = 0; i < CPATCHAS_KEYWORD.length; i++) {
            if (content.contains(CPATCHAS_KEYWORD[i])) {
                isCaptchasMessage = true;
                break;
            }
        }
        return isCaptchasMessage;
    }

    public static boolean isCaptchasMessageEn(String content) {
        Boolean isCaptchasMessage = false;
        for (int i = 0; i < CPATCHAS_KEYWORD_EN.length; i++) {
            if (content.contains(CPATCHAS_KEYWORD_EN[i])) {
                isCaptchasMessage = true;
                break;
            }
        }
        return isCaptchasMessage;
    }

    public static String getResultText(String company, String captchas, Context context) {
        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(company)) {
             builder.append(String.format(context.getString(R.string.code_from),company));
        } else {
            builder.append(context.getString(R.string.code_is));
        }
        builder.append(captchas);
        return builder.toString();
    }

    public static String getContentInBracket(String str, String address) {
        Pattern pattern = Pattern.compile("\\【(.*?)\\】");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if (matcher.group(1) != null && matcher.group(1).length() < 10) {

                return analyseSpecialCompany(matcher.group(1), str, address);
            }
        }
        Pattern pattern1 = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher1 = pattern1.matcher(str);
        while (matcher1.find()) {
            if (matcher1.group(1) != null && matcher1.group(1).length() < 10) {

                return analyseSpecialCompany(matcher1.group(1), str, address);
            }
        }
        Pattern pattern2 = Pattern.compile("\\((.*?)\\)");
        Matcher matcher2 = pattern2.matcher(str);
        while (matcher2.find()) {
            if (matcher2.group(1) != null && matcher2.group(1).length() < 10) {

                return analyseSpecialCompany(matcher2.group(1), str, address);
            }
        }
        return "";
    }

    private static String analyseSpecialCompany(String company, String content, String address) {
        String companyName = company;
        if (company.equals("掌淘科技")) {
            int index = content.indexOf("的验证码");
            companyName = content.substring(0, index);
            companyName = companyName.replaceAll("【掌淘科技】", "").trim();
        } else {
            if (content.contains("贝壳单词的验证码")) {
                companyName = "贝壳单词";
            }
        }
        if (address.equals("10010")) {
            companyName = "中国联通";
        }
        if (address.equals("10086")) {
            companyName = "中国移动";
        }
        if (address.equals("10000")) {
            companyName = "中国电信";
        }
        return companyName;
    }

    public static String getCaptchas (String formattedMessage){
        if (!isContainsChinese(formattedMessage)) {
            if (isCaptchasMessageEn(formattedMessage) && !tryToGetCaptchasEn(formattedMessage).equals("")) {
                return tryToGetCaptchasEn(formattedMessage);
            }
        } else if (isCaptchasMessage(formattedMessage) && !tryToGetCaptchas(formattedMessage).equals("")) {
            return tryToGetCaptchas(formattedMessage);
        }
        return "";
    }

}