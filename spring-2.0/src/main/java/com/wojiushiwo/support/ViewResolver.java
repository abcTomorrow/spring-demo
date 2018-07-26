package com.wojiushiwo.support;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewResolver {
    private String fileName;
    private File file;

    public ViewResolver(String fileName, File file) {
        this.fileName = fileName;
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public String parse(ModelAndView mv) throws Exception{

        StringBuffer sb = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.file, "r");

        try{
            String line = null;
            while(null != (line = ra.readLine())){
                Matcher m = matcher(line);
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i ++) {
                        String paramName = m.group(i);
                        Object paramValue = mv.getModel().get(paramName);
                        if(null == paramValue){ continue; }
                        line = line.replaceAll("@\\{" + paramName + "\\}", paramValue.toString());
                    }
                }

                sb.append(line);
            }
        }finally{
            ra.close();
        }
        return sb.toString();
    }
    private Matcher matcher(String str){
        Pattern pattern = Pattern.compile("@\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(str);
        return m;
    }
}
