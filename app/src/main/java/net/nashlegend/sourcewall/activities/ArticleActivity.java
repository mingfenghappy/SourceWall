package net.nashlegend.sourcewall.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import net.nashlegend.sourcewall.App;
import net.nashlegend.sourcewall.R;
import net.nashlegend.sourcewall.adapters.ArticleDetailAdapter;
import net.nashlegend.sourcewall.data.Config;
import net.nashlegend.sourcewall.data.Consts.Extras;
import net.nashlegend.sourcewall.data.Consts.RequestCode;
import net.nashlegend.sourcewall.data.Mob;
import net.nashlegend.sourcewall.dialogs.FavorDialog;
import net.nashlegend.sourcewall.dialogs.InputDialog;
import net.nashlegend.sourcewall.dialogs.ReportDialog;
import net.nashlegend.sourcewall.dialogs.ReportDialog.ReportReasonListener;
import net.nashlegend.sourcewall.events.ArticleFinishLoadingLatestRepliesEvent;
import net.nashlegend.sourcewall.events.ArticleStartLoadingLatestRepliesEvent;
import net.nashlegend.sourcewall.events.Emitter;
import net.nashlegend.sourcewall.model.Article;
import net.nashlegend.sourcewall.model.UComment;
import net.nashlegend.sourcewall.request.ResponseObject;
import net.nashlegend.sourcewall.request.SimpleCallBack;
import net.nashlegend.sourcewall.request.api.ArticleAPI;
import net.nashlegend.sourcewall.request.api.MessageAPI;
import net.nashlegend.sourcewall.request.api.UserAPI;
import net.nashlegend.sourcewall.simple.SimpleSubscriber;
import net.nashlegend.sourcewall.util.AutoHideUtil;
import net.nashlegend.sourcewall.util.AutoHideUtil.AutoHideListener;
import net.nashlegend.sourcewall.util.RegUtil;
import net.nashlegend.sourcewall.util.ShareUtil;
import net.nashlegend.sourcewall.util.ToastUtil;
import net.nashlegend.sourcewall.util.UiUtil;
import net.nashlegend.sourcewall.util.UrlCheckUtil;
import net.nashlegend.sourcewall.util.Utils;
import net.nashlegend.sourcewall.view.MediumListItemView;
import net.nashlegend.sourcewall.view.common.LoadingView;
import net.nashlegend.sourcewall.view.common.LoadingView.ReloadListener;
import net.nashlegend.sourcewall.view.common.listview.LListView;
import net.nashlegend.sourcewall.view.common.listview.LListView.OnRefreshListener;

import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class ArticleActivity extends BaseActivity implements OnRefreshListener, OnClickListener,
        ReloadListener {

    private LListView listView;
    private ArticleDetailAdapter adapter;
    private Article article;
    private LoadingView loadingView;
    private String notice_id;
    private AdapterView.OnItemClickListener onItemClickListener;
    private FloatingActionsMenu floatingActionsMenu;
    private ProgressBar progressBar;
    private boolean loadDesc = false;
    private Menu menu;
    private AppBarLayout appbar;
    private int headerHeight = 112;
    /**
     * 是否倒序加载已经加载完成了所有的回贴
     */
    private boolean hasLoadAll = false;

    public ArticleActivity() {
        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onReplyItemClick(view, position, id);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.layout_operation);
        FloatingActionButton replyButton = (FloatingActionButton) findViewById(R.id.button_reply);
        FloatingActionButton recomButton = (FloatingActionButton) findViewById(
                R.id.button_recommend);
        FloatingActionButton favorButton = (FloatingActionButton) findViewById(R.id.button_favor);
        loadingView = (LoadingView) findViewById(R.id.article_progress_loading);
        progressBar = (ProgressBar) findViewById(R.id.article_loading);
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
        listView = (LListView) findViewById(R.id.list_detail);
        Mob.onEvent(Mob.Event_Open_Article);
        loadingView.setReloadListener(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setOnClickListener(new OnClickListener() {

            boolean preparingToScrollToHead = false;

            @Override
            public void onClick(View v) {
                if (preparingToScrollToHead) {
                    listView.setSelection(0);
                } else {
                    preparingToScrollToHead = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            preparingToScrollToHead = false;
                        }
                    }, 200);
                }
            }
        });
        article = getIntent().getParcelableExtra(Extras.Extra_Article);
        notice_id = getIntent().getStringExtra(Extras.Extra_Notice_Id);
        if (!TextUtils.isEmpty(article.getSubjectName())) {
            setTitle(article.getSubjectName());
        }
        adapter = new ArticleDetailAdapter(this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClickListener);
        listView.setCanPullToLoadMore(false);
        listView.setOnRefreshListener(this);

        if (replyButton != null) {
            replyButton.setOnClickListener(this);
        }
        if (recomButton != null) {
            recomButton.setOnClickListener(this);
        }
        if (favorButton != null) {
            favorButton.setOnClickListener(this);
        }

        headerHeight = (int) getResources().getDimension(R.dimen.actionbar_height);
        AutoHideUtil.applyListViewAutoHide(this, listView,
                (int) getResources().getDimension(R.dimen.actionbar_height), autoHideListener);
        floatingActionsMenu.setVisibility(View.GONE);
        loadData(-1);

        Emitter.register(this);
    }

    @Override
    protected void onDestroy() {
        Emitter.unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(ArticleStartLoadingLatestRepliesEvent event) {
        if (event.article != null && article != null && Utils.equals(event.article.getId(),
                article.getId())) {
            onStartLoadingLatest();
        }
    }

    public void onEventMainThread(ArticleFinishLoadingLatestRepliesEvent event) {
        if (event.article != null && article != null && Utils.equals(event.article.getId(),
                article.getId())) {
            onFinishLoadingLatest();
        }
    }

    /**
     * @param offset -1是指刷新
     */
    private void loadData(int offset) {
        if (offset < 0) {
            loadFromArticle();
        } else {
            loadReplies(offset);
        }
    }

    private void replyArticle() {
        replyArticle(null);
    }

    private void replyArticle(UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
        } else {
            Intent intent = new Intent(this, Config.getReplyActivity());
            intent.putExtra(Extras.Extra_Ace_Model, article);
            if (comment != null) {
                intent.putExtra(Extras.Extra_Simple_Comment, comment);
            }
            startOneActivityForResult(intent, RequestCode.Code_Reply_Article);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.Code_Reply_Article && resultCode == RESULT_OK && !loadDesc) {
            article.setCommentNum(article.getCommentNum() + 1);
            listView.startLoadingMore();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void recommend() {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
        } else {
            Mob.onEvent(Mob.Event_Recommend_Article);
            InputDialog.Builder builder = new InputDialog.Builder(this);
            builder.setTitle(R.string.recommend_article);
            builder.setCancelable(true);
            builder.setCanceledOnTouchOutside(false);
            builder.setOnClickListener(new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        InputDialog d = (InputDialog) dialog;
                        confirmRecommend(d.InputString);
                    }
                }
            });
            InputDialog inputDialog = builder.create();
            inputDialog.show();
        }
    }

    private void confirmRecommend(String comment) {
        ArticleAPI.recommendArticle(article.getId(), article.getTitle(), article.getSummary(),
                comment, new SimpleCallBack<Boolean>() {
                    @Override
                    public void onFailure() {
                        toast(R.string.recommend_failed);
                    }

                    @Override
                    public void onSuccess() {
                        toast(R.string.recommend_ok);
                    }
                });
    }

    private void favor() {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
        } else {
            // basket dialog
            Mob.onEvent(Mob.Event_Favor_Article);
            new FavorDialog.Builder(this).setTitle(R.string.action_favor).create(article).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article, menu);
        this.menu = menu;
        setMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_load_acs:
                startLoadAcs();
                break;
            case R.id.action_load_desc:
                startLoadDesc();
                break;
            case R.id.action_share_to_wechat_circle:
                Mob.onEvent(Mob.Event_Share_Article_To_Wechat_Circle);
                ShareUtil.shareToWeiXinCircle(App.getApp(), article.getUrl(), article.getTitle(),
                        article.getSummary(), null);
                break;
            case R.id.action_share_to_wechat_friends:
                Mob.onEvent(Mob.Event_Share_Article_To_Wechat_friend);
                ShareUtil.shareToWeiXinFriends(App.getApp(), article.getUrl(), article.getTitle(),
                        article.getSummary(), null);
                break;
            case R.id.action_share_to_weibo:
                Mob.onEvent(Mob.Event_Share_Article_To_Weibo);
                ShareUtil.shareToWeibo(this, article.getUrl(), article.getTitle(),
                        article.getSummary(), null);
                break;
            case R.id.action_open_in_browser:
                if (!TextUtils.isEmpty(article.getUrl())) {
                    Mob.onEvent(Mob.Event_Open_Article_In_Browser);
                    UrlCheckUtil.openWithBrowser(article.getUrl());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void replyComment(UComment comment) {
        replyArticle(comment);
    }

    private void likeComment(final MediumListItemView mediumListItemView) {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
            return;
        }
        if (mediumListItemView.getData().isHasLiked()) {
            toastSingleton(getString(R.string.has_liked_this));
            return;
        }
        final UComment comment = mediumListItemView.getData();
        ArticleAPI.likeComment(comment.getID(), new SimpleCallBack<Boolean>() {
            @Override
            public void onSuccess() {
                comment.setHasLiked(true);
                comment.setLikeNum(comment.getLikeNum() + 1);
                if (mediumListItemView.getData() == comment) {
                    mediumListItemView.plusOneLike();
                }
            }
        });
    }

    private void reportComment(final UComment uComment) {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
            return;
        }
        new ReportDialog.Builder(this)
                .setTitle("举报")
                .setReasonListener(new ReportReasonListener() {
                    @Override
                    public void onGetReason(final Dialog dia, String reason) {
                        ArticleAPI.reportReply(uComment.getID(), reason,
                                new SimpleCallBack<Boolean>() {
                                    @Override
                                    public void onFailure() {
                                        ToastUtil.toastBigSingleton("举报未遂……");
                                    }

                                    @Override
                                    public void onSuccess() {
                                        UiUtil.dismissDialog(dia);
                                        ToastUtil.toastBigSingleton("举报成功");
                                    }
                                });
                    }
                })
                .create()
                .show();
    }

    private void deleteComment(final UComment comment) {
        if (!UserAPI.isLoggedIn()) {
            gotoLogin();
            return;
        }
        ArticleAPI.deleteMyComment(comment.getID(), new SimpleCallBack<Boolean>() {
            @Override
            public void onFailure() {
                toastSingleton("删除失败~");
            }

            @Override
            public void onSuccess() {
                if (article.getCommentNum() > 0) {
                    article.setCommentNum(article.getCommentNum() - 1);
                }
                adapter.remove(comment);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void copyComment(UComment comment) {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        manager.setPrimaryClip(
                ClipData.newPlainText(null, RegUtil.html2PlainText(comment.getContent())));
        toast(R.string.copy_success);
    }

    private void onReplyItemClick(final View view, int position, long id) {
        if (view instanceof MediumListItemView) {
            final UComment comment = ((MediumListItemView) view).getData();
            final ArrayList<String> ops = new ArrayList<>();
            ops.add(getString(R.string.action_reply));
            ops.add(getString(R.string.action_copy));
            if (!comment.isHasLiked()) {
                ops.add(getString(R.string.action_like));
            }
            if (!comment.getAuthor().getId().equals(UserAPI.getUserID())) {
                ops.add(getString(R.string.report));
            }
            System.out.println(comment.getAuthor().getId());
            System.out.println(UserAPI.getUserID());
            if (comment.getAuthor().getId().equals(UserAPI.getUserID())) {
                ops.add(getString(R.string.action_delete));
            }
            String[] operations = new String[ops.size()];
            for (int i = 0; i < ops.size(); i++) {
                operations[i] = ops.get(i);
            }
            new AlertDialog.Builder(this)
                    .setItems(operations, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which >= ops.size() || which < 0) {
                                return;
                            }
                            String desc = ops.get(which);
                            if (desc.equals(getString(R.string.action_reply))) {
                                replyComment(comment);
                            } else if (desc.equals(getString(R.string.action_copy))) {
                                copyComment(comment);
                            } else if (desc.equals(getString(R.string.action_like))) {
                                likeComment((MediumListItemView) view);
                            } else if (desc.equals(getString(R.string.action_delete))) {
                                deleteComment(comment);
                            } else if (desc.equals(getString(R.string.report))) {
                                reportComment(comment);
                            }
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_reply:
                replyArticle();
                break;
            case R.id.button_recommend:
                recommend();
                break;
            case R.id.button_favor:
                favor();
                break;
        }
    }

    @Override
    public void reload() {
        adapter.clear();
        loadData(-1);
    }

    @Override
    public void onStartRefresh() {
        loadData(-1);
    }

    @Override
    public void onStartLoadMore() {
        loadData(adapter.getCount() - 1);
    }

    /**
     * 倒序查看
     */
    public void startLoadDesc() {
        Mob.onEvent(Mob.Event_Reverse_Read_Article);
        loadDesc = true;
        loadingView.onLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Article) {
            article = (Article) adapter.getList().get(0);
            article.setDesc(loadDesc);
            adapter.clear();
            adapter.add(article);
            loadData(0);
        } else {
            adapter.clear();
            loadData(-1);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 正序查看
     */
    private void startLoadAcs() {
        Mob.onEvent(Mob.Event_Normal_Read_Article);
        loadDesc = false;
        loadingView.onLoading();
        listView.setCanPullToLoadMore(false);
        setMenuVisibility();
        if (adapter.getCount() > 0 && adapter.getList().get(0) instanceof Article) {
            article = (Article) adapter.getList().get(0);
            article.setDesc(loadDesc);
            adapter.clear();
            adapter.add(article);
            loadData(0);
        } else {
            adapter.clear();
            loadData(-1);
        }
        adapter.notifyDataSetChanged();
    }

    private void setMenuVisibility() {
        if (menu != null) {
            if (loadDesc) {
                menu.findItem(R.id.action_load_acs).setVisible(true);
                menu.findItem(R.id.action_load_desc).setVisible(false);
            } else {
                menu.findItem(R.id.action_load_acs).setVisible(false);
                menu.findItem(R.id.action_load_desc).setVisible(true);
            }
        }
    }

    private void loadFromArticle() {
        if (!TextUtils.isEmpty(notice_id)) {
            MessageAPI.ignoreOneNotice(notice_id);
            notice_id = null;
        }
        ArticleAPI
                .getArticleDetail(article.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSubscriber<ResponseObject<Article>>() {
                    @Override
                    public void onNext(ResponseObject<Article> result) {
                        if (isFinishing()) {
                            return;
                        }
                        if (result.ok) {
                            progressBar.setVisibility(View.VISIBLE);
                            floatingActionsMenu.setVisibility(View.VISIBLE);
                            loadingView.onSuccess();
                            Article tmpArticle = result.result;
                            tmpArticle.setUrl(article.getUrl());
                            tmpArticle.setSummary(article.getSummary());
                            tmpArticle.setCommentNum(article.getCommentNum());
                            article = tmpArticle;
                            adapter.clear();
                            adapter.add(article);
                            adapter.notifyDataSetChanged();
                            loadReplies(0);
                        } else {
                            if (result.statusCode == 404) {
                                toastSingleton(R.string.article_404);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                toastSingleton(getString(R.string.load_failed));
                                loadingView.onFailed();
                            }
                        }
                    }
                });
    }

    private void loadReplies(int offset) {
        int limit = 20;
        if (loadDesc) {
            //因为无法保证获取回复的数据，所以只能采取一次全部加载的方式，但是又不能超过5000，这是服务器的限制
            if (article.getCommentNum() <= 0) {
                limit = 4999;
                offset = 0;
                hasLoadAll = true;
            } else {
                int tmpOffset = article.getCommentNum() - offset - 20;
                if (tmpOffset <= 0) {
                    hasLoadAll = true;
                    limit = 20 + tmpOffset;
                    tmpOffset = 0;
                } else {
                    hasLoadAll = false;
                }
                offset = tmpOffset;
            }
        }
        ArticleAPI
                .getArticleReplies(article.getId(), offset, limit)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        loadingView.onSuccess();
                        listView.doneOperation();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseObject<ArrayList<UComment>>>() {
                    @Override
                    public void onCompleted() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNext(ResponseObject<ArrayList<UComment>> result) {
                        if (isFinishing()) {
                            return;
                        }
                        if (result.ok) {
                            loadingView.onSuccess();
                            if (result.result.size() > 0) {
                                if (loadDesc) {
                                    adapter.addAllReversely(result.result);
                                } else {
                                    adapter.addAll(result.result);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            if (loadDesc) {
                                hasLoadAll = false;
                            }
                            if (result.statusCode == 404) {
                                toastSingleton(R.string.article_404);
                                finish();
                            } else {
                                toastSingleton(getString(R.string.load_failed));
                                loadingView.onFailed();
                            }
                        }
                        if (adapter.getCount() > 0) {
                            listView.setCanPullToLoadMore(true);
                        } else {
                            listView.setCanPullToLoadMore(false);
                        }
                        if (loadDesc && hasLoadAll) {
                            article.setCommentNum(adapter.getCount() - 1);
                            listView.setCanPullToLoadMore(false);
                        }
                        listView.doneOperation();
                    }
                });
    }

    private void onStartLoadingLatest() {
        listView.setCanPullToLoadMore(false);
        menu.findItem(R.id.action_load_acs).setVisible(false);
        menu.findItem(R.id.action_load_desc).setVisible(false);
    }

    private void onFinishLoadingLatest() {
        if (adapter.getCount() > 0) {
            listView.setCanPullToLoadMore(true);
        } else {
            listView.setCanPullToLoadMore(false);
        }
        if (loadDesc && hasLoadAll) {
            listView.setCanPullToLoadMore(false);
        }
        setMenuVisibility();
    }

    private AutoHideListener autoHideListener = new AutoHideListener() {
        AnimatorSet backAnimatorSet;
        AnimatorSet hideAnimatorSet;

        @Override
        public void animateHide() {
            if (backAnimatorSet != null && backAnimatorSet.isRunning()) {
                backAnimatorSet.cancel();
            }
            if (hideAnimatorSet == null || !hideAnimatorSet.isRunning()) {
                hideAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(appbar, "translationY",
                        appbar.getTranslationY(), -headerHeight);
                ObjectAnimator header2Animator = ObjectAnimator.ofFloat(progressBar, "translationY",
                        progressBar.getTranslationY(), -headerHeight);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu,
                        "translationY", floatingActionsMenu.getTranslationY(),
                        floatingActionsMenu.getHeight());
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(header2Animator);
                animators.add(footerAnimator);
                hideAnimatorSet.setDuration(300);
                hideAnimatorSet.playTogether(animators);
                hideAnimatorSet.start();
            }
        }

        @Override
        public void animateBack() {
            if (hideAnimatorSet != null && hideAnimatorSet.isRunning()) {
                hideAnimatorSet.cancel();
            }
            if (backAnimatorSet == null || !backAnimatorSet.isRunning()) {
                backAnimatorSet = new AnimatorSet();
                ObjectAnimator headerAnimator = ObjectAnimator.ofFloat(appbar, "translationY",
                        appbar.getTranslationY(), 0f);
                ObjectAnimator header2Animator = ObjectAnimator.ofFloat(progressBar, "translationY",
                        progressBar.getTranslationY(), 0f);
                ObjectAnimator footerAnimator = ObjectAnimator.ofFloat(floatingActionsMenu,
                        "translationY", floatingActionsMenu.getTranslationY(), 0f);
                ArrayList<Animator> animators = new ArrayList<>();
                animators.add(headerAnimator);
                animators.add(header2Animator);
                animators.add(footerAnimator);
                backAnimatorSet.setDuration(300);
                backAnimatorSet.playTogether(animators);
                backAnimatorSet.start();
            }
        }
    };
}
