package com.example.sourcewall.util;

import com.example.sourcewall.AppApplication;
import com.example.sourcewall.R;
import com.example.sourcewall.model.SubItem;

import java.util.ArrayList;

/**
 * Created by NashLegend on 2014/9/23 0023
 */
public class Consts {

    public static final String Action_Open_Content_Fragment = "sourcewall.action.open.content.fragment";

    public static final String Action_Open_Articles_Fragment = "sourcewall.action.open.articles.fragment";
    public static final String Action_Open_Posts_Fragment = "sourcewall.action.open.posts.fragment";
    public static final String Action_Open_Questions_Fragment = "sourcewall.action.open.questions.fragment";

    // usually used in intent.put
    public static final String Extra_Ace_Model = "sourcewall.extra.ace.model.id";
    public static final String Extra_Article = "sourcewall.extra.article.id";
    public static final String Extra_Simple_Comment = "sourcewall.extra.simple.comment.id";
    public static final String Extra_Post = "sourcewall.extra.post.id";
    public static final String Extra_Question = "sourcewall.extra.question.id";
    public static final String Extra_Answer = "sourcewall.extra.answer.id";
    public static final String Extra_SubItem = "sourcewall.extra.subitem.id";

    // usually used in SharedPreferences
    public static final String Key_Cookie = "sourcewall.key.cookie";
    public static final String Key_Access_Token = "sourcewall.key.access.token";
    public static final String Key_Ukey = "sourcewall.key.ukey";
    public static final String Key_Is_Night_Mode = "sourcewall.key.is.night.mode";

    // login webpage
    public static final String LOGIN_URL = "https://account.guokr.com/sign_in/?display=mobile";
    public static final String SUCCESS_URL_1 = "http://m.guokr.com/";
    public static final String SUCCESS_URL_2 = "http://www.guokr.com/";
    public static final String Cookie_Token_Key = "_32353_access_token";
    public static final String Cookie_Ukey_Key = "_32353_ukey";


    public static ArrayList<SubItem> getSections() {
        SubItem[] items = {new SubItem(SubItem.Section_Article, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.article), "Article"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.post), "Post"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Collections, AppApplication.getApplication().getResources().getString(R.string.question), "Question")};
        ArrayList<SubItem> subItems = new ArrayList<SubItem>();
        for (int i = 0; i < items.length; i++) {
            subItems.add(items[i]);
        }
        return subItems;
    }

    public static ArrayList<SubItem> getArticles() {
        SubItem[] items = {new SubItem(SubItem.Section_Article, SubItem.Type_Collections, "科学人", ""),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "热点", "hot"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "前沿", "frontier"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "评论", "review"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "专访", "interview"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "视觉", "visual"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "速读", "brief"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "谣言粉碎机", "fact"),
                new SubItem(SubItem.Section_Article, SubItem.Type_Single_Channel, "商业科技", "techb")
        };
        //FIXME，暂时先无学科。
        ArrayList<SubItem> subItems = new ArrayList<SubItem>();
        for (int i = 0; i < items.length; i++) {
            subItems.add(items[i]);
        }
        return subItems;
    }

    //new SubItem(SubItem.Section_Post, SubItem.Type_Private_Channel, "我的小组", "user_group"),
    public static ArrayList<SubItem> getPosts() {
        SubItem[] items = {
                new SubItem(SubItem.Section_Post, SubItem.Type_Collections, "小组热贴", "hot_posts"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "谣言粉碎机", "40"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "DIY", "27"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "自然控", "36"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "死理性派", "39"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "Geek笑点低", "63"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "吃货研究所", "69"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "谋杀 现场 法医", "31"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "美丽也是技术活", "73"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "情感夜夜话", "127"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "心事鉴定组", "33"),
                new SubItem(SubItem.Section_Post, SubItem.Type_Single_Channel, "Hello world!", "60"),
        };
        //FIXME，前十而已
        ArrayList<SubItem> subItems = new ArrayList<SubItem>();
        for (int i = 0; i < items.length; i++) {
            subItems.add(items[i]);
        }
        return subItems;
    }

    public static ArrayList<SubItem> getQuestions() {
        SubItem[] items = {new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "热门问答", "hottest"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Collections, "精彩回答", "highlight"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生活", "生活"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生物", "生物"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "健康", "健康"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "医学", "医学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "心理学", "心理学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "物理学", "物理学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "化学", "化学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "物理", "物理"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "生物学", "生物学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "社会科学", "社会科学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "互联网", "互联网"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "数学", "数学"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "我好想问", "我好想问"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "食物", "食物"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "运动", "运动"),
                new SubItem(SubItem.Section_Question, SubItem.Type_Single_Channel, "计算机", "计算机"),
        };
        ArrayList<SubItem> subItems = new ArrayList<SubItem>();
        for (int i = 0; i < items.length; i++) {
            subItems.add(items[i]);
        }
        return subItems;
    }


}
