package com.daebbang.daebbangcommon.success;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    USER_LOGIN(200, "로그인에 성공하였습니다."),
    USER_LOGOUT(200, "로그아웃에 성공하였습니다."),
    USER_ID_AVAILABLE(200, "사용 가능한 아이디입니다."),
    USER_RETRIEVED(200, "회원 조회에 성공하였습니다."),
    USER_UPDATED(200, "회원 정보 수정에 성공하였습니다."),
    USER_UPDATED_PASSWORD(200, "회원 비밀번호 수정에 성공하였습니다."),
    USER_JOINED(201, "회원 가입에 성공하였습니다."),
    USER_WITHDRAWN(200, "회원 탈퇴에 성공하였습니다."),

    ADDRESS_SAVED(201, "새 주소 등록에 성공하였습니다."),
    ADDRESS_RETRIEVED(200, "주소 목록 조회에 성공하였습니다."),
    ADDRESS_UPDATED(200, "주소 수정에 성공하였습니다."),
    ADDRESS_DELETED(200, "주소 삭제에 성공하였습니다."),

    SEND_EMAIL(201, "이메일 전송에 성공하였습니다."),

    ADD_CART(201, "장바구니 추가에 성공하였습니다."),
    UPDATE_CART(200, "장바구니 수정에 성공하였습니다."),
    DELETE_CART(200, "장바구니 삭제에 성공하였습니다."),

    VERIFY_AUTH_CODE(200, "핸드폰 인증에 성공하였습니다."),

    CHECK_WISH_LIST(200, "위시리스트 여부 조회에 성공하였습니다."),
    ADD_WISH_LIST(201, "위시리스트 추가에 성공하였습니다."),
    DELETE_WISH_LIST(200, "위시리스트 삭제에 성공하였습니다."),

    ORDER_PREPARED(200, "주문이 생성되었습니다."),
    ORDER_CONFIRMED(200, "결제가 완료되었습니다."),
    ORDER_CANCELLED(200, "주문이 취소되었습니다."),
    ORDER_COMPLETED(200, "구매 확정이 완료되었습니다."),
    ORDER_LIST_RETRIEVED(200, "주문 목록 조회에 성공하였습니다."),
    ORDER_DETAIL_RETRIEVED(200, "주문 상세 조회에 성공하였습니다."),
    ORDER_STATUS_COUNT_RETRIEVED(200, "배송 현황 건수 조회에 성공하였습니다."),

    REVIEW_CREATED(201, "리뷰가 등록되었습니다."),
    REVIEW_UPDATED(200, "리뷰가 수정되었습니다."),
    REVIEW_DELETED(200, "리뷰가 삭제되었습니다."),
    REVIEW_LIST_RETRIEVED(200, "리뷰 목록 조회에 성공하였습니다."),
    REVIEW_STATS_RETRIEVED(200, "리뷰 통계 조회에 성공하였습니다."),

    POINT_BALANCE_RETRIEVED(200, "적립금 잔액 조회에 성공하였습니다."),
    POINT_HISTORY_RETRIEVED(200, "적립금 내역 조회에 성공하였습니다.");

    private final int status;
    private final String message;
}
