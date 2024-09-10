function KGInigis() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/KGInigisOrderValidCheck";
    var paramData = {
        itemId: $("#itemId").val(),
        count: $("#count").val()
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: param,
        beforeSend: function(xhr) {
            /* 데이터 전송하기 전에 헤더에 csrf 값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        cache: false,
        success: function(result, status) {
            if (result.PayDto.merchant_uid) {
                // 주문이 성공적으로 완료되었으면 KG 이니시스 결제를 진행합니다.
                processKGInigisOrder(result.PayDto, token, header);
            } else {
                alert("주문 처리 중 오류가 발생했습니다.");
            }
        },
        error: function(jqXHR, status, error) {
            if (jqXHR.status == '401') {
                alert('로그인 후 이용해주세요.');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}

$(document).ready(function() {
    IMP.init('imp22180164'); // 포트원 관리자 콘솔에서 발급받은 가맹점 식별코드

    $('#KGInigis').on('click', function() {
        KGInigis();
    });
});

function generateMerchantUid() {
    var uid = 'merchant_' + Math.random().toString(36).substr(2, 9);
    return uid.toString(); // 문자열 형변환 추가
}

function processKGInigisOrder(orderData, token, header) {

    payment_uid = generateMerchantUid(); // 고유한 merchant_uid 생성


    IMP.request_pay({
        pg: 'html5_inicis', // KG 이니시스 결제
        pay_method: 'card',
        merchant_uid: payment_uid,
        name: orderData.payName,
        amount: orderData.payAmount,
                       buyer_email: orderData.buyerEmail,
        buyer_name: orderData.buyerName,
        buyer_tel: orderData.buyerTel,
        buyer_addr: orderData.buyerAddr,
        buyer_postcode: orderData.buyerPostcode
    }, function(rsp) { // callback
        if (rsp.success) {
            alert('결제가 완료되었습니다.');
            // 결제 성공 시 서버에 결제 정보를 전송하여 저장하는 로직 추가
            // savePaymentInfo(rsp, orderData.id, token, header);
        } else {
            if (rsp.error_msg) {
                alert('결제에 실패하였습니다. 에러 내용: ' + rsp.error_msg);
            } else {
                alert('결제가 취소되었습니다.');
            }
            cancelOrder(orderData.merchant_uid, token, header);
        }
    });
}

function cancelOrder(merchant_uid, token, header) {
    fetch('/KGInigisOrderDELETE', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        body: JSON.stringify({ orderId: merchant_uid }),
    })
    .then(response => response.json())
    .then(result => {
        if (result === 'Payment failed, order cancelled') {
            alert('주문이 취소되었습니다.');
            location.href = '/';
        }
    })
    .catch(error => {
        //alert('주문 취소 중 오류가 발생했습니다: ' + error);
    });
}
