document.addEventListener('DOMContentLoaded', () => {
    var isSearchQueryNotEmpty = /*[[${not #strings.isEmpty(itemSearchDto.searchQuery)}]]*/ false;

    function initializeInfiniteScroll(isSearch) {
        let page = 1;
        let isLoading = false;

        const csrfToken = $('meta[name="_csrf"]').attr('content');
        const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

        function loadItems() {
            if (isLoading) return;
            isLoading = true;
            console.log(`Loading items for page ${page}`);

            const url = isSearch ? `/loadItems/search?page=${page}` : `/loadItems?page=${page}`;

            $.ajax({
                url: url,
                method: 'GET',
                dataType: 'json',
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                },
                success: function(data) {
                    console.log('Data loaded successfully:', data);
                    data.content.forEach(item => {
                        $('#product-row').append(`
                            <div class="col">
                                <div class="card">
                                    <a href="/item/${item.id}" class="text-dark">
                                        <img src="${item.imgUrl}" class="card-img-top" alt="${item.itemNm}" height="300">
                                        <div class="card-body">
                                            <h4 class="card-title">${item.itemNm}</h4>
                                            <h3 class="card-title text-danger">${item.price}원</h3>
                                            <p class="card-text">${item.stockNumber}개</p>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        `);
                    });
                    page++;
                    console.log(`Next page will be ${page}`);
                    isLoading = false;

                    if (!data.last) {
                        const lastItem = $('#product-row .col:last')[0];
                        if (lastItem) {
                            observer.observe(lastItem);
                        }
                    } else {
                        observer.disconnect();
                    }
                },
                error: function(error) {
                    console.error('Error loading data:', error);
                    isLoading = false;
                }
            });
        }

        loadItems();

        const observerOptions = {
            root: null,
            threshold: 0.5
        };

        const observer = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    observer.unobserve(entry.target);
                    loadItems();
                }
            });
        }, observerOptions);
    }

    initializeInfiniteScroll(isSearchQueryNotEmpty);

    const slides = document.querySelector('.slides');
    const slideCount = document.querySelectorAll('.slide').length;
    const slideWidth = document.querySelector('.slide').offsetWidth;
    let currentIndex = 0;

    function showSlide(index) {
        if (index >= slideCount) {
            slides.style.transition = 'none';
            currentIndex = 0;
            slides.style.transform = `translateX(-${currentIndex * slideWidth}px)`;
            setTimeout(() => {
                slides.style.transition = 'transform 0.5s ease-in-out';
            }, 50);
        } else if (index < 0) {
            slides.style.transition = 'none';
            currentIndex = slideCount - slideCount / 2;
            slides.style.transform = `translateX(-${currentIndex * slideWidth}px)`;
            setTimeout(() => {
                slides.style.transition = 'transform 0.5s ease-in-out';
            }, 50);
        } else {
            currentIndex = index;
            slides.style.transform = `translateX(-${currentIndex * slideWidth}px)`;
        }
    }

    document.querySelector('.prev').addEventListener('click', () => {
        showSlide(currentIndex - 1);
    });

    document.querySelector('.next').addEventListener('click', () => {
        showSlide(currentIndex + 1);
    });

    setInterval(() => {
        showSlide(currentIndex + 1);
    }, 3000);
});

/*사이드바 위치설정*/
document.addEventListener("DOMContentLoaded", function() {
    const sidebar = document.querySelector('.sidebar');
    const triggerHeight = 200; // 사이드바가 나타나기 시작할 스크롤 위치 (픽셀 단위)

    function checkScroll() {
        if (window.scrollY > triggerHeight) {
            sidebar.classList.add('visible');
        } else {
            sidebar.classList.remove('visible');
        }
    }

    window.addEventListener('scroll', checkScroll);
});

document.addEventListener('DOMContentLoaded', () => {
    // 최근 본 상품을 API로부터 가져오는 함수
    function fetchRecentProducts() {
        return $.ajax({
            url: '/recentViews',
            method: 'GET',
            dataType: 'json'
        });
    }

    // 최근 본 상품을 사이드바에 표시하는 함수
    function displayRecentItems() {
        fetchRecentProducts()
            .then(recentProducts => {
                const sidebarList = document.querySelector('.sidebar ul');
                sidebarList.innerHTML = ''; // 기존 목록을 비웁니다

                recentProducts.forEach(product => {
                    const listItem = document.createElement('li');
                    listItem.innerHTML = `
                        <a href="/item/${product.productId}">
                            <img src="${product.imageUrl}" alt="${product.itemNm}" width="100">
                            <span>${product.itemNm}</span>
                        </a>
                    `;
                    sidebarList.appendChild(listItem);
                });
            })
            .catch(error => {
                console.error('Error fetching recent products:', error);
            });
    }

    // 페이지 로드 시 최근 본 상품 표시
    displayRecentItems();
});