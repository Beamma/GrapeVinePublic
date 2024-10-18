//Using CHAT-GPT to help with using localStorage to save the selected colour scheme
window.addEventListener('DOMContentLoaded', () => {
    const theme = localStorage.getItem('theme');
    if (theme === 'original-scheme') {
        document.documentElement.classList.add('original-scheme')
    }
})
//Toggles the colour scheme between the old and new colour scheme
document.addEventListener('keydown', function(event) {
    if (event.ctrlKey && event.shiftKey && event.key === 'G') {
        document.documentElement.classList.toggle('original-scheme');

        //To save the selected theme to the localstorage
        if (document.documentElement.classList.contains('original-scheme')) {
            localStorage.setItem('theme', 'original-scheme');
        } else {
            localStorage.removeItem('theme')
        }
    }
});