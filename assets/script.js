// Mobile nav
const navToggle = document.getElementById("navToggle");
const navLinks = document.getElementById("navLinks");

if (navToggle && navLinks) {
  navToggle.addEventListener("click", () => {
    const isOpen = navLinks.classList.toggle("open");
    navToggle.setAttribute("aria-expanded", String(isOpen));
  });

  // Close on link click (mobile)
  navLinks.querySelectorAll("a").forEach((a) => {
    a.addEventListener("click", () => {
      navLinks.classList.remove("open");
      navToggle.setAttribute("aria-expanded", "false");
    });
  });
}

// Active section highlight (scrollspy-ish)
const links = Array.from(document.querySelectorAll(".nav-link"));
const sections = links
  .map((l) => document.querySelector(l.getAttribute("href")))
  .filter(Boolean);

const setActive = () => {
  const y = window.scrollY + 120; // header offset
  let activeIndex = 0;

  for (let i = 0; i < sections.length; i++) {
    const s = sections[i];
    if (s.offsetTop <= y) activeIndex = i;
  }

  links.forEach((l, i) => l.classList.toggle("active", i === activeIndex));
};

window.addEventListener("scroll", setActive, { passive: true });
window.addEventListener("load", setActive);
