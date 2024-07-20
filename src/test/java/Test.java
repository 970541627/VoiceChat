public class Test {
    public static void main(String[] args) {
        // 嵌套for循环 结束

        //数字类型 + 文字类型 =  文字类型
        //数字类型 + 数字类型 = 数字类型
        //文字类型 + 文字类型 = 文字类型

        //1*1 = 1
        //2*1 = 1,2*2=4
        //3*1 = 3,3*2=6 , 3*3=9

        //左边是i，右边是j ，
        //判断条件 j<=i, i<=9
        //变量初始化 i=1, j=1
        //cout<<i<<"*"<<j<<"="<<i*j<<",";
        //里面的循环结束之后，换行 cout<<""<<endl;


        byte[] s = new byte[4]; //14 -->  0x 00 00 00 0E
        int l=200;
        s[0] = (byte) ((l>>0)&255);
        s[1] =(byte) ((l>>8)&255);
        s[2] = (byte) ((l>>16)&255);
        s[3] = (byte) ((l>>24)&255);

        int f=(s[0]<<0)|(s[1]<<8)|(s[2]<<16)|(s[3]<<24);
        System.out.println("s等于：" + f);
        // 数组

        // char 字符类型 写文字
        // 1、 char可以用来存二进制数据
        // 2、 什么是字节
        // 3、 如何将char转换为int等其他类型（long , long long,short ,double,float）


    }
}
