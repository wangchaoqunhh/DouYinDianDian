package com.wcq.douyindiandian.entity

class MainDataBean {
    //是否在直播页关注 榜上大哥
    var isLiveAttention: Boolean = false
    var attentionMixYinLang: Int = 0

    //是否在关注好友页取消 关注的好友
    var isCancelAttention: Boolean = false


    //是否开启一键养号
    var isOpenCultivateAccount: Boolean = false

    //是否刷首页推荐
    var isBrushHomeRecommend: Boolean = false

    //首页推荐完成时间
    var homeRecommendCompleteTime: Int = 0
    var homeRecommendCompleteNum: Int = 0

    //是否刷同城推荐
    var isBrushCityRecommend: Boolean = false

    //同城推荐完成时间
    var cityRecommendCompleteTime: Int = 0

    //是否在直播页随机评论
    var isLiveRandomComment: Boolean = false

    //观看直播的数量
    var seeLiveNum: Int = 0

    //每个直播观看的时间 分钟
    var seeLiveTime: Int = 0

    //随机评论的内容
    var commentContent: String? = null

    //评论完成的个数
    var commentCompletedNum: Int = 0

    //是否开启看视频
    var isWatchVideo: Boolean = false

    //要看视频的数量
    var seeVideoNum: Int = 0

    //看视频完成的个数
    var seeVideoCompletedNum: Int = 0

    //是否开启看热搜.榜单.挑战
    var isTopSearch: Boolean = false

    //看热搜.榜单.挑战 的数量
    var topSearchNum: Int = 0

    //看热搜.榜单.挑战 的完成数量
    var topSearchCompletedNum: Int = 0

    //是否随机在直播页关注几个主播
    var isRandomFollow: Boolean = false

    //关注主播数量
    var randomFollowNum: Int = 0

    //关注主播完成数量
    var randomFollowCompletedNum: Int = 0
}