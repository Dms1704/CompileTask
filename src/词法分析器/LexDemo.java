package 词法分析器;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.HashMap;

public class LexDemo extends Application {

    private GridPane gridPane = new GridPane() ;
    private char ch ;
    private HashMap<String, Integer> imSeedCode;      //记录关键字种别码
    private char[] input = new char[255] ;          //输入字符串
    private String taken ;          //单词缓冲区
    private String put = "" ;            //输出
    private TextArea putTextArea = new TextArea() ;      //输出文本框
    private final int digitCode = 23 ;      //数字编码
    private final int signCode = 24 ;       //标识符编码

    public void seedCodeInit(){         //初始化关键字种别码
        this.imSeedCode = new HashMap<>() ;
        imSeedCode.put("BEGIN", 1) ;
        imSeedCode.put("END", 2) ;
        imSeedCode.put("VAR", 3) ;
        imSeedCode.put("INTEGER", 4) ;
        imSeedCode.put("WHILE", 5) ;
        imSeedCode.put("IF", 6) ;
        imSeedCode.put("THEN", 7) ;
        imSeedCode.put("ELSE", 8) ;
        imSeedCode.put("DO", 9) ;
        imSeedCode.put("PROCEDURE", 10) ;
        imSeedCode.put(":", 11) ;
        imSeedCode.put(";", 12) ;
        imSeedCode.put("+", 13) ;
        imSeedCode.put("*", 14) ;
        imSeedCode.put("<", 15) ;
        imSeedCode.put(">", 16) ;
        imSeedCode.put("<=", 17) ;
        imSeedCode.put(">=", 18) ;
        imSeedCode.put("<>", 19) ;
        imSeedCode.put("=", 20) ;
        imSeedCode.put("(", 21) ;
        imSeedCode.put(")", 22) ;
    }

    public void init(){         //初始化
        seedCodeInit() ;
        gridPane.setPadding(new Insets(8, 8, 8, 8));
        gridPane.setHgap(5) ;
        gridPane.setVgap(5) ;
    }

    public void conCat(){       //拼接字符串
        taken += ch ;
    }

    public boolean isLetter(){      //判断是否是大写字母
        if (ch >= 'A'&&ch <= 'Z')
            return true;
        else
            return false;
    }

    public boolean isDigit(){
        if(ch >= '0' && ch <= '9'){
            return true ;
        }
        else
            return false ;
    }

    public int reserve(){       //判断taken是否是关键字
        if(imSeedCode.get(taken) != null){
            return imSeedCode.get(taken) ;
        }
        else
            return signCode ;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene ;
        BorderPane borderPane = new BorderPane() ;

        //程序输入框
        TextArea textArea = new TextArea() ;
        textArea.setEditable(true); // 设置多行输入框能否编辑
        textArea.setPromptText("请输入程序"); // 设置多行输入框的提示语
        textArea.setWrapText(true); // 设置多行输入框是否支持自动换行。true表示支持，false表示不支持。
        textArea.setStyle("-fx-min-height: 150 ;");
        borderPane.setTop(textArea) ;

        //输出框
        putTextArea.setEditable(true);
        putTextArea.setWrapText(true);
        putTextArea.setStyle("-fx-min-height: 150 ;");
        borderPane.setBottom(putTextArea);

        //按钮
        Button button = new Button("分析") ;
        button.setOnMouseClicked(e->buttonClick(textArea.getText()));     //添加事件
        borderPane.setCenter(button);

        scene = new Scene(borderPane, 500, 400) ;
        stage.setScene(scene);
        scene.getStylesheets().add("style.css");;
        stage.setTitle("词法分析器");
        stage.show();
    }

    public void buttonClick(String input){
        this.input = input.toCharArray() ;
        init() ;
        lex() ;
        put += "分析完毕" ;
        putTextArea.setText(put);
    }

    //词法扫描程序
    public void lex(){

        int i ;
        for(i=-1; i<input.length;){
            taken = "" ;
            if(i+1 >= input.length)
                break;
            else
                ch = input[++i] ;       //读取下个字符

            while(ch == ' ' || ch == 10)    //去空格
                ch = input[++i] ;

            if(isLetter()){         //判断是不是关键字
                conCat();
                if(i+1 >= input.length) {
//                    System.out.println("("+taken+","+reserve()+")");
                    put += "("+taken+","+reserve()+")\n" ;
                    return;
                }
                else
                    ch = input[++i] ;       //读取下个字符
                while(isDigit() || isLetter()){
                    conCat();
                    if(i+1 >= input.length){
//                        System.out.println("("+taken+","+reserve()+")");
                        put += "("+taken+","+reserve()+")\n" ;
                        return;
                    }
                    else
                        ch = input[++i] ;       //读取下个字符
                }
                ch = input[--i] ;       //回退一个字符
//                System.out.println("("+taken+","+reserve()+")");
                put += "("+taken+","+reserve()+")\n" ;
            }

            else if(isDigit()){     //判断是不是数字
                conCat();
                if(i+1 >= input.length) {
//                    System.out.println("("+taken+","+digitCode+")");
                    put += "("+taken+","+digitCode+")\n" ;
                    return;
                }
                else
                    ch = input[++i] ;       //读取下个字符
                while (isDigit()){
                    conCat() ;
                    if(i+1 >= input.length) {
//                        System.out.println("("+taken+","+digitCode+")");
                        put += "("+taken+","+digitCode+")\n" ;
                        return;
                    }
                    else
                        ch = input[++i] ;       //读取下个字符
                }
                ch = input[--i] ;         //回退一个字符
//                System.out.println("("+taken+","+digitCode+")");
                put += "("+taken+","+digitCode+")\n" ;
            }
            else
                switch (ch){
                    case '>':
                        conCat();
                        if(i+1 >= input.length) {
//                            System.out.println("("+taken+","+reserve()+")");
                            put += "("+taken+","+reserve()+")\n" ;
                            return;
                        }
                        else
                            ch = input[++i] ;       //读取下个字符

                        if(ch == '='){
                            conCat();
//                            System.out.println("("+taken+","+reserve()+")");
                            put += "("+taken+","+reserve()+")\n" ;
                            break;
                        }
                        else {
                            ch = input[--i];
//                            System.out.println("(" + taken + "," + reserve() + ")");
                            put += "("+taken+","+reserve()+")\n" ;
                            break;
                        }
                    case '<':
                        conCat();
                        if(i+1 >= input.length) {
//                            System.out.println("("+taken+","+reserve()+")");
                            put += "("+taken+","+reserve()+")\n" ;
                            return;
                        }
                        else
                            ch = input[++i] ;       //读取下个字符

                        if(ch == '=' || ch== '>'){
                            conCat();
//                            System.out.println("("+taken+","+reserve()+")");
                            put += "("+taken+","+reserve()+")\n" ;
                            break;
                        }
                        else {
                            ch = input[--i] ;
//                            System.out.println("("+taken+","+reserve()+")");
                            put += "("+taken+","+reserve()+")\n" ;
                            break;
                        }
                    default:
                        conCat();
//                        System.out.println("("+taken+","+reserve()+")");
                        put += "("+taken+","+reserve()+")\n" ;
                        break;
                }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}