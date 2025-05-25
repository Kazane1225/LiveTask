function togglePassword(iconSpan) {
  const container = iconSpan.closest('.password-container');
  const input = container.querySelector('input');
  const icon = iconSpan.querySelector("i");
  if(input.type === "password") {
    input.type = "text";
    icon.classList.remove("fa-eye");
    icon.classList.add("fa-eye-slash");
  } else {
    input.type = "password";
    icon.classList.remove("fa-eye-slash");
    icon.classList.add("fa-eye");
  }
}