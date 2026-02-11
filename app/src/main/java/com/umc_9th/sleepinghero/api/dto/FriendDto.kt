package com.umc_9th.sleepinghero.api.dto

/**
 * GET /friends/requests - 대기 중인 친구 요청 목록 조회
 */
data class FriendRequestsResponse(
    val requests: List<FriendRequestItem>
)

data class FriendRequestItem(
    val memberId: Int,
    val nickname: String,
    val profilePicture: String
)

/**
 * GET /friends - 친구 목록 조회 (승인된 친구)
 */
data class FriendsListResponse(
    val friends: List<FriendItem>
)

data class FriendItem(
    val memberId: Int,
    val nickname: String,
    val profilePicture: String
)

/**
 * GET /friends/ranking - 친구 수면 랭킹 조회
 */
data class FriendRankingResponse(
    val rankings: List<FriendRankingItem>
)

data class FriendRankingItem(
    val nickname: String,
    val totalSleepTime: String,
    val rank: Int
)

/**
 * PATCH /friends/requests/{status} - 친구 요청 수락/거절
 * status: APPROVE 또는 REJECTED
 */
data class FriendRequestStatusRequest(
    val nickname: String
)

/**
 * POST /friends/requests - 친구 요청 보내기
 */
data class SendFriendRequestRequest(
    val nickname: String
)