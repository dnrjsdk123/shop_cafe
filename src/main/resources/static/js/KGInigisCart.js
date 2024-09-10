function orders() {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var url = "/KGInigisOrderCartValidCheck";

    var dataList = [];
    $("input[name=cartChkBox]:checked").each(function() {
        var cartItemId = $(this).val();
        var count = $("#count_" + cartItemId).val();
        if (cartItemId && count) {
            dataList.push({ itemId: cartItemId, count: count });
        }
    });

    var param = JSON.stringify(dataList);

    $.ajax({
        url: url,
        type: "POST",
        contentType: "application/json",
        data: param,
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        dataType: "json",
        success: function(result) {
            if (result.status === 'success') {
                var payDto = result.PayDto;
                initiateKGInicisPayment(payDto);
            } else {
                alert(result.message);
            }
        },
        error: function(jqXHR) {
            if (jqXHR.status === 401) {
                alert('로그인 후 이용해주세요.');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}

function initiateKGInicisPayment(payDto) {
    var IMP = window.IMP; // 생략 가능
    IMP.init('imp22180164'); // 가맹점 식별코드

    IMP.request_pay({
        pg: 'html5_inicis', // PG사
        pay_method: 'card', // 결제 수단
        merchant_uid: payDto.merchant_uid, // 주문번호
        name: payDto.payName, // 결제할 상품명
        amount: payDto.payAmount, // 결제 금액
        buyer_email: payDto.buyerEmail,
        buyer_name: payDto.buyerName,
        buyer_tel: payDto.buyerTel,
        buyer_addr: payDto.buyerAddr,
        buyer_postcode: payDto.buyerPostcode
    }, function (rsp) { // 콜백 함수
        if (rsp.success) {
            alert('결제가 완료되었습니다.');
            // 결제 성공 처리
            location.href = "/orders"; // 성공 후 이동할 페이지
        } else {
            alert('결제에 실패하였습니다. 에러 내용: ' + rsp.error_msg);
            // 결제 실패 처리
            location.href = "/cart"; // 실패 후 이동할 페이지
        }
    });
}