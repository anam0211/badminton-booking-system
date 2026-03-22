// 1. Slider Banner (Tự động trượt ảnh)
const initSlider = () => {
    const track = document.getElementById('sliderTrack');
    if (!track || track.children.length === 0) return;

    let index = 0;
    setInterval(() => {
        index = (index + 1) % track.children.length;
        track.style.transform = `translateX(-${index * 100}%)`;
    }, 5000);
};

// 2. Tắt Spinner và chạy Slider khi trang đã tải xong hoàn toàn
window.addEventListener('load', () => {
    const spinner = document.getElementById('spinner-overlay');
    if (spinner) {
        spinner.style.display = 'none';
    }
    // Khởi chạy slider
    initSlider();
});