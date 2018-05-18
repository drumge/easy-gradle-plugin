package com.drumge.easy.plugin.api

class EasyUtils {

    private static String osName = ''
    /**
     * 正则表达式中根据不同系统取文件间隔符, 比如 String.split() 或者 String.replaceAll() 中
     * @return
     */
    static String regSeparator() {
        if (isStringEmpty(osName) ) {
            osName = System.getProperty('os.name')
        }
        if (osName.startsWith('Windows')) {
            return '\\\\'
        }
        return File.separator
    }

    /**
     * 根据.class文件获取类的名字
     * @param classPath
     * @return
     */
    static String getClassSimpleName(String classPath){
        if(isStringEmpty(classPath)){
            return ""
        }
        String pack = classPath.replace('.class', '').replaceAll('[.]', regSeparator())
        pack = pack.substring(pack.lastIndexOf(File.separator) + 1)
        return pack
    }

    /**
     * 首字母大写转换
     * @param str
     * @return
     */
    static String upperFirstCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    static boolean isStringEmpty(String text) {
        return text == null || text.length() == 0
    }

}