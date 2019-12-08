package 语法分析实验;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Production{

    private char left ;
    private String right ;

    public Production(char left, String right){
        this.left = left ;
        this.right = right ;
    }

    public char getLeft() {
        return left;
    }

    public void setLeft(char left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return right;
    }
}

public class ParseDemo extends Application {

    private Production[] pros = {
            new Production('A', "CB"),
            new Production('B', "+CB"),
            new Production('B', "$"),
            new Production('C', "ED"),
            new Production('D', "$"),
            new Production('D', "*ED"),
            new Production('E', "i"),
            new Production('E', "(A)")
    };  //产生式集合

    private HashSet<Character> finalCh = new HashSet<>();    //终结符集合
    private HashSet<Character> notFinalCh = new HashSet<>(); //非终结符集合
    private Production[][] analysisTable;
    /* 测试用例
    private Production[][] analysisTable = {
                {null, null, new Production('A', "CB"), null, new Production('A', "CB"), null},
                {new Production('B',"+CB"), null, null, new Production('A', ""), null, new Production('A', "")},
                {null, null, new Production('C', "ED"), null, new Production('C', "ED"), null},
                {new Production('D', ""), new Production('D', "*ED"), null, new Production('D', ""), null, new Production('D', "")},
                {null, null, new Production('E', "(A)"), null, new Production('E', "i"), null}
        };  //预测分析表*/

    //总控程序相关变量
    private int cnt = pros.length;                     //产生式总个数
    private String s;
    private Stack<Character> input = new Stack() ;     //输入串
    private Stack<Character> stack = new Stack() ;     //分析栈

    //First集合相关变量
    private HashSet<Character> firsts[] = new HashSet[pros.length];

    //Follow集合相关变量
    private HashSet<Character> follows[] = new HashSet[pros.length];

    //UI相关变量
    private TextArea putTextArea = new TextArea();      //输出文本框
    private String put;            //输出
    private GridPane parseGridPane = new GridPane();    //总控程序显示信息

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * 按钮事件
     */
    public void buttonClick(String str){
        s = replaceStr(str);

        //设置新舞台需要的UI变量
        Stage stage = new Stage();
        Scene scene;
        BorderPane borderPane = new BorderPane();
        Label col1 = new Label("分析栈");
        Label col2 = new Label("输入串");
        Label col3 = new Label("分析过程");
        col1.setStyle("-fx-font-size: 20");
        col2.setStyle("-fx-font-size: 20");
        col3.setStyle("-fx-font-size: 20");

        //设置显示信息列名
        ColumnConstraints column1 = new ColumnConstraints(100, 150, 300);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, 300);
        ColumnConstraints column3 = new ColumnConstraints(100, 150, 300);
        parseGridPane.getColumnConstraints().addAll(column1, column2, column3);

        //设置parseGridPane的样式
        parseGridPane.setPadding(new Insets(5, 5, 5, 5));
        parseGridPane.setVgap(5);
        parseGridPane.setHgap(8);
        parseGridPane.setStyle("-fx-alignment: center; -fx-text-alignment: center");

        //添加列名
        parseGridPane.add(col1, 0, 0);
        parseGridPane.add(col2, 1, 0);
        parseGridPane.add(col3, 2, 0);

        getFirst('A');
        getFirst('B');
        getFirst('C');
        getFirst('D');
        getFirst('E');
        getFollow('A');
        getFollow('B');
        getFollow('C');
        getFollow('D');
        getFollow('E');
        initFinalAndNonFinal();
        getTable();
        parse();

        borderPane.setCenter(parseGridPane);
        scene = new Scene(borderPane, 450, 500);
        stage.setScene(scene);
        stage.setTitle("分析过程");
        stage.show();
    }

    /**
     * 消除字符串的空格、回车、换行符、制表符
     * @param str
     * @return 消除空格后的字符串
     */
    public String replaceStr(String str) {

        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 打印信息
     */
    public void printInfo(){
        int i, j;
        put += "First集如下\n";
        for (i=0; i<5; i++){
            put += "First["+i+"] = {";
            for (Character c:firsts[i]){
                put += c+" ";
            }
            put += "}\n";
        }
        put += "Follow集如下:\n";
        for (i=0; i<5; i++){
            put += "Follow["+i+"] = {";
            for (Character c:follows[i]){
                put += c+" ";
            }
            put += "}\n";
        }
    }

    /**
     * 构造预测分析表
     */
    public void getTable(){
        int i, j;
        char ch;

        //初始化分析表
        analysisTable = new Production[notFinalCh.size()][finalCh.size()+1];
        for (i=0; i<notFinalCh.size(); i++){
            for (j=0; j<finalCh.size(); j++){
                analysisTable[i][j] = null;
            }
        }

        //构造
        for (i=0; i<cnt; i++){
            ch = pros[i].getRight().charAt(0);
            //若右部第一个字符为终结符
            if (isFinalCh(ch)){
                //若不是空串
                if (ch != '$'){
                    analysisTable[getNonFinalIndex(pros[i].getLeft())][getFinalIndex(ch)] = new Production(pros[i].getLeft(), pros[i].getRight());
                }
                if (ch == '$'){
                    for (Character c:follows[getNonFinalIndex(pros[i].getLeft())]){
                        analysisTable[getNonFinalIndex(pros[i].getLeft())][getFinalIndex(c)] = new Production(pros[i].getLeft(), pros[i].getRight());
                    }
                }
            }
            //若右部第一个字符为非终结符
            else {
                for (Character c:firsts[getNonFinalIndex(ch)]){
                    analysisTable[getNonFinalIndex(pros[i].getLeft())][getFinalIndex(c)] = new Production(pros[i].getLeft(), pros[i].getRight());
                }
                //若有空串将左部的非终结符的Follow集合加进来
                if (firsts[getNonFinalIndex(ch)].contains('$')){
                    for (Character c:follows[getNonFinalIndex(ch)]){
                        analysisTable[getNonFinalIndex(pros[i].getLeft())][getFinalIndex(c)] = new Production(pros[i].getLeft(), pros[i].getRight());
                    }
                }
            }
        }
    }

    /**
     * 初始化终结符集合和非终结符集合
     */
    public void initFinalAndNonFinal(){
        int i, j, t;
        char ch;
        for (i=0; i<pros.length; i++){
            notFinalCh.add(pros[i].getLeft());
        }
        for (i=0; i<pros.length; i++){
            for (j=0; j<pros[i].getRight().length(); j++){
                ch = pros[i].getRight().charAt(j);
                if (ch != '$'){
                    t = 0;
                    for (Character c:notFinalCh){
                        if (c == ch){       //若此右部字符为非终结符则break
                            break;
                        }
                        t++;
                    }
                    //ch和非终结符都不相同则加入终结符集合
                    if (t == notFinalCh.size()){
                        finalCh.add(ch);
                    }
                }
            }
        }
/*        System.out.println("终结符集合长度:"+finalCh.size());
        System.out.println("非终结符长度:"+notFinalCh.size());
        for (Character c:finalCh){
            System.out.println(c);
        }*/
    }

    /**
     * 求非终结符x的Follow集合
     * @param x
     */
    public void getFollow(char x){
        int flag = 0;       //空串标志:0表示无空串

        int i, j;
        char r;
        char next;
        follows[getNonFinalIndex(x)] = new HashSet<>();  //为每一个Arraylist申请空间，不然会有空指向异常

        if (x == 'A'){
            follows[getNonFinalIndex(x)].add('#');
        }
        for (i=0; i<cnt; i++){
            int index = -1;
            int len = pros[i].getRight().length();
            //找到非终结符x出现的位置
            for (j=0; j<len; j++){
                if(pros[i].getRight().charAt(j) == x){
                    index = j;
                    break;
                }
            }

            //如果找到了,并且它不是最后一个字符
            if (index != -1 && index < len-1){
                next = pros[i].getRight().charAt(index+1);

                //如果下一个字符是终结符，添加进x的Follow集
                if (isFinalCh(next)){
                    follows[getNonFinalIndex(x)].add(next);
                }
                //若为非终结符
                else {
                    for (Character c:firsts[getNonFinalIndex(next)]){
                        //将除去空串的First[next]集合添加到Follow[x]中
                        if (c != '$'){
                            follows[getNonFinalIndex(x)].add(c);
                        }
                        else {
                            flag = 1;
                        }
                    }

                    //如果有空串并且左部不是它本身(防止陷入死循环),当前非终结符的Follow集是x的Follow集
                    //First集合为空其实就是结尾
                    r = pros[i].getLeft();
                    if (flag == 1 && r != x){
                        getFollow(r);
                        for (Character c:follows[getNonFinalIndex(r)]){
                            follows[getNonFinalIndex(x)].add(c);
                        }
                    }
                }
            }
            //如果找到了但x在右部产生式的末尾
            else if (index == len-1 && x != pros[i].getLeft()){
                r = pros[i].getLeft();
                getFollow(r);
                for (Character c:follows[getNonFinalIndex(r)]){
                    follows[getNonFinalIndex(x)].add(c);
                }
            }
        }
    }

    /**
     * 求非终结符x的First集合
     * @param x
     */
    public void getFirst(char x){
        int flag = 0;       //空串标志:0表示无空串
        int tot = 0;        //记录每条产生式有空串的非终结符的个数
        /**
         * 举例说明tot
         * 若一条产生式为A->BCD
         *      若B中有空串则tot++
         *      若C有空串则tot++
         *      .....
         */

        int i, j;
        char r;
        firsts[getNonFinalIndex(x)] = new HashSet<>();
        for (i=0; i< cnt; i++){
            if (pros[i].getLeft() == x){
                //若右部第一个字符为终结符则加入First集合
                if (isFinalCh(pros[i].getRight().charAt(0))){
                    firsts[getNonFinalIndex(x)].add(pros[i].getRight().charAt(0));
                }
                else {
                    for (j=0; j<pros[i].getRight().length(); j++){
                        r = pros[i].getRight().charAt(j);
                        //若遇到终结符
                        if (isFinalCh(r)){
                            firsts[getNonFinalIndex(x)].add(r);
                            break;
                        }
                        //求此非终结符的First集合
                        getFirst(r);
                        for (Character c: firsts[getNonFinalIndex(r)]){
                            if (c == '$'){
                                flag = 1;
                            }
                            else {
                                firsts[(getNonFinalIndex(x))].add(c);
                            }
                        }
                        //没空串就退出
                        if (flag == 0){
                            break;
                        }
                        else {
                            flag = 1;
                            tot++;
                        }
                    }
                    if (tot == pros[i].getRight().length()){
                        firsts[getNonFinalIndex(x)].add('$');
                    }
                }
            }
        }
    }

    /**
     * 判断改字符是否为终结符
     * @param x
     * @return 1 是终结符
     * @return 0 是非终结符
     */
    public boolean isFinalCh(char x){
        for (Production pro:pros){
            if (x == pro.getLeft()){
                return false;
            }
        }
        return true;
    }

    /**
     * 获得终结符在预测分析表中的下标
     * @param c
     * @return 0 1 2...
     */
    public int getFinalIndex(char c){
        switch (c){
            case '+':return 0;
            case '*':return 1;
            case '(':return 2;
            case ')':return 3;
            case 'i':return 4;
            case '#':return 5;
        }
        return -1;
    }

    /**
     * 通过下标获得终结符
     * @param index
     * @return
     *
     */
    public char getFinalCh(int index){
        switch (index){
            case 0:return '+';
            case 1:return '*';
            case 2:return '(';
            case 3:return ')';
            case 4:return 'i';
            case 5:return '#';
        }
        return '0';
    }

    /**
     * 获得非终结符在预测分析表中的下标
     * @param c
     * @return 0 1 2...
     */
    public int getNonFinalIndex(char c){
        switch (c){
            case 'A':return 0;
            case 'B':return 1;
            case 'C':return 2;
            case 'D':return 3;
            case 'E':return 4;
        }
        return -1;
    }

    /**
     * 语法分析总程序
     */
    public void parse(){
        int i;
        int rowcount = 1;

        //初始化
        input.push('#');
        for(i=s.length()-1;i>=0;i--){
            input.push(s.charAt(i));
        }

        stack.push('#');
        stack.push('A');

        while (input.size() > 0){
            //输出分析栈内容
            String outputs = "";
            for(i=0;i<stack.size();i++){
                outputs+=stack.get(i);
            }
            parseGridPane.add(new Label(outputs), 0, rowcount);

            //输出剩余输入串内容
            outputs = "" ;
            for(i=0;i<input.size();i++){
                outputs+=input.get(i);
            }
            parseGridPane.add(new Label(outputs), 1, rowcount);

            char c = stack.get(stack.size()-1);    //指向分析栈栈顶字符
            char x = input.get(input.size()-1);    //指向输入串栈顶字符

            if (c == x && c == '#'){
                parseGridPane.add(new Label("Accept"), 2, rowcount);
                rowcount++;
                return;
            }
            else if (c == x && c != '#'){           //终结符匹配
                parseGridPane.add(new Label("终结符匹配"), 2, rowcount);
                rowcount++;
                input.pop();
                stack.pop();
            }
            else {
                if (getFinalIndex(x) != -1 && analysisTable[getNonFinalIndex(c)][getFinalIndex(x)] != null){

                    String s = analysisTable[getNonFinalIndex(c)][getFinalIndex(x)].getRight();
                    parseGridPane.add(new Label("按产生式:"+analysisTable[getNonFinalIndex(c)][getFinalIndex(x)].getLeft()+"->"+
                            analysisTable[getNonFinalIndex(c)][getFinalIndex(x)].getRight()), 2, rowcount);
                    rowcount++;
                    stack.pop();
                    //若为空串则不入栈任何东西
                    if (s != "$") {
                        for (i=s.length()-1; i>=0; i--){
                            stack.push(s.charAt(i));
                        }
                    }
                }
                else if (getFinalIndex(x) == -1){
                    parseGridPane.add(new Label("输入串含未知字符"), 2, rowcount);
                    rowcount++;
                    return;
                }
                else if (analysisTable[getNonFinalIndex(c)][getFinalIndex(x)] == null){
                    parseGridPane.add(new Label("输入串不合法"), 2, rowcount);
                    rowcount++;
                    return;
                }

            }
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene ;
        BorderPane borderPane = new BorderPane() ;

        //程序输入框
        TextArea textArea = new TextArea() ;
        textArea.setEditable(true); // 设置多行输入框能否编辑
        textArea.setPromptText("请输入待分析字符串"); // 设置多行输入框的提示语
        textArea.setWrapText(true); // 设置多行输入框是否支持自动换行。true表示支持，false表示不支持。
        textArea.setStyle("-fx-min-height: 150 ;");
        borderPane.setTop(textArea) ;

        //输出框
        putTextArea.setEditable(true);
        putTextArea.setWrapText(true);
        putTextArea.setStyle("-fx-min-height: 150 ;");
        borderPane.setBottom(putTextArea);

        //按钮
        HBox hBox = new HBox();
        hBox.setStyle("-fx-text-alignment: center; -fx-alignment: center");
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.setSpacing(20);    //设置控件间间距
        Button getButton = new Button("查看相关集合");
        getButton.setOnMouseClicked(e->{
            put = "";
            getFirst('A');
            getFirst('B');
            getFirst('C');
            getFirst('D');
            getFirst('E');
            getFollow('A');
            getFollow('B');
            getFollow('C');
            getFollow('D');
            getFollow('E');
            printInfo();
            putTextArea.setText(put);
        });
        Button proButton = new Button("查看产生式");
        proButton.setOnMouseClicked(e->{
            put = "";
            put +=  "A->CB\n" +
                    "B->+CB\n" +
                    "B->$\n" +
                    "C->ED\n" +
                    "D->*ED\n" +
                    "D->$\n" +
                    "E->(A)\n" +
                    "E->i";
            putTextArea.setText(put);
        });
        Button button = new Button("分析") ;
        button.setOnMouseClicked(e->buttonClick(textArea.getText()));     //添加事件
        Button tableButton = new Button("查看预测分析表");
        tableButton.setOnMouseClicked(e->tableButtonClick());

        hBox.getChildren().add(proButton);
        hBox.getChildren().add(getButton);
        hBox.getChildren().add(tableButton);
        hBox.getChildren().add(button);
        borderPane.setCenter(hBox);

        scene = new Scene(borderPane, 500, 400) ;
        stage.setScene(scene);
/*        scene.getStylesheets().add("style.css");;*/
        stage.setTitle("语法分析器");
        stage.show();
    }

    /**
     * 按钮事件:展示预测分析表
     */
    public void tableButtonClick(){
        BorderPane borderPane = new BorderPane();
        GridPane gridPane = new GridPane();
        Stage tableStage = new Stage();
        Scene tableScene;

        //设置gridpane的样式
        gridPane.setPadding(new Insets(8, 8, 8, 8));
        gridPane.setStyle("-fx-font-size: 15; -fx-text-alignment: center; -fx-alignment: center");
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        ColumnConstraints column1 = new ColumnConstraints(100);
        ColumnConstraints column2 = new ColumnConstraints(50, 150, 300);
        ColumnConstraints column3 = new ColumnConstraints(50, 150, 300);
        ColumnConstraints column4 = new ColumnConstraints(50, 150, 300);
        ColumnConstraints column5 = new ColumnConstraints(50, 150, 300);
        ColumnConstraints column6 = new ColumnConstraints(50, 150, 300);
        ColumnConstraints column7 = new ColumnConstraints(50, 150, 300);
        gridPane.getColumnConstraints().addAll(column1, column2, column3, column4, column5, column6, column7);

        HBox titleHbox = new HBox();
        titleHbox.setStyle("-fx-text-alignment: center; -fx-alignment: center");

        //先获得First和Follow集合
        getFirst('A');
        getFirst('B');
        getFirst('C');
        getFirst('D');
        getFirst('E');
        getFollow('A');
        getFollow('B');
        getFollow('C');
        getFollow('D');
        getFollow('E');
        initFinalAndNonFinal();
        getTable();

        int i, j;
        //预测分析表显示
        Label title = new Label("预测分析表");
        title.setStyle("-fx-alignment: center");
        title.setStyle("-fx-font-size: 20");

        gridPane.add(new Label("非终结符"), 0, 0);
        //填非终结符列
        for (i=1; i<notFinalCh.size()+1; i++){
            for (j=0; j<finalCh.size()+1; j++){
                if (analysisTable[i-1][j] != null){
                    gridPane.add(new Label(Character.toString(analysisTable[i-1][j].getLeft())), 0, i);
                    break;
                }
            }
        }
        //填终结符列
        for (i=0; i<finalCh.size()+1; i++){
            gridPane.add(new Label(Character.toString(getFinalCh(i))), i+1, 0);
        }
        //填主表数据
        for (i=1; i<notFinalCh.size()+1; i++){
            for (j=1; j<finalCh.size()+2; j++){
                if (analysisTable[i-1][j-1] != null){
                    gridPane.add(new Label(analysisTable[i-1][j-1].toString()), j, i);
                }
                else {
                    gridPane.add(new Label("null"), j, i);
                }
            }
        }
        titleHbox.getChildren().add(title);
        borderPane.setTop(titleHbox);
        borderPane.setCenter(gridPane);

        //新建一个舞台窗口放预测分析表
        tableScene = new Scene(borderPane, 600, 350);
        tableStage.setScene(tableScene);
        tableStage.setTitle("预测分析表");
        tableStage.show();
    }

}
