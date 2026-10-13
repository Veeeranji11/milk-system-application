// Village Milk Collection and Money Management System - Client Script
// Bilingual (English & Telugu) Support & Live Calculations

const translations = {
    en: {
        systemTitle: "Village Milk System",
        subtitle: "Milk Collection & Money Management",
        dashboard: "Dashboard",
        farmers: "Farmers",
        collections: "Milk Collections",
        payments: "Money & Payments",
        reports: "Reports Hub",
        logout: "Logout",
        totalFarmers: "Total Farmers",
        todayMilk: "Today's Milk",
        todayPayout: "Today's Payout",
        totalDisbursed: "Total Paid Out",
        pendingBalance: "Pending Balance",
        monthlyMilk: "This Month Milk",
        quickEntry: "Quick Milk Entry",
        farmer: "Farmer",
        selectFarmer: "-- Select Registered Farmer --",
        shift: "Shift",
        morning: "Morning",
        evening: "Evening",
        date: "Date",
        litres: "Milk Litres (L)",
        fatPercent: "Fat %",
        ratePerLitre: "Rate per Litre",
        totalAmount: "Total Amount",
        saveEntry: "Save Collection Entry",
        recentCollections: "Recent Collections",
        viewAll: "View All",
        actions: "Actions",
        passbook: "Passbook",
        edit: "Edit",
        delete: "Delete",
        payNow: "Pay Now",
        recordPayment: "Record Payment",
        cash: "Cash",
        upi: "UPI",
        bankTransfer: "Bank Transfer",
        paidAmount: "Paid Amount",
        totalEarned: "Total Earned",
        mySupplies: "My Milk Deliveries",
        changePassword: "Change Password",
        profile: "My Profile",
        cattle: "Cattle / Buffalos",
        phone: "Phone Number",
        village: "Village",
        bankDetails: "Bank Details",
        welcomeFarmer: "Welcome Farmer",
        farmerId: "Farmer ID",
        print: "Print Statement"
    },
    te: {
        systemTitle: "గ్రామ పాల కేంద్రం",
        subtitle: "పాల సేకరణ & నగదు నిర్వహణ",
        dashboard: "డ్యాష్‌బోర్డ్",
        farmers: "రైతులు",
        collections: "పాల సేకరణ",
        payments: "నగదు చెల్లింపులు",
        reports: "నివేదికలు (Reports)",
        logout: "లాగ్ అవుట్",
        totalFarmers: "మొత్తం రైతులు",
        todayMilk: "నేటి పాలు",
        todayPayout: "నేటి చెల్లింపు",
        totalDisbursed: "చెల్లించిన మొత్తం",
        pendingBalance: "బకాయి మొత్తం",
        monthlyMilk: "ఈ నెల పాలు",
        quickEntry: "త్వరిత పాల నమోదు",
        farmer: "రైతు",
        selectFarmer: "-- నమోదైన రైతును ఎంచుకోండి --",
        shift: "పూట (Shift)",
        morning: "ఉదయం",
        evening: "సాయంత్రం",
        date: "తేదీ",
        litres: "పాల లీటర్లు (L)",
        fatPercent: "వెన్న శాతం (Fat %)",
        ratePerLitre: "లీటరు ధర (₹)",
        totalAmount: "మొత్తం ధర (₹)",
        saveEntry: "నమోదును భద్రపరచండి",
        recentCollections: "ఇటీవలి పాల సేకరణలు",
        viewAll: "అన్నీ చూడండి",
        actions: "చర్యలు",
        passbook: "పాస్‌బుక్",
        edit: "సవరించు",
        delete: "తొలగించు",
        payNow: "చెల్లించండి",
        recordPayment: "చెల్లింపు నమోదు",
        cash: "నగదు (Cash)",
        upi: "UPI చెల్లింపు",
        bankTransfer: "బ్యాంక్ బదిలీ",
        paidAmount: "చెల్లించిన మొత్తం",
        totalEarned: "మొత్తం ఆదాయం",
        mySupplies: "నా పాల సరఫరా",
        changePassword: "పాస్‌వర్డ్ మార్చు",
        profile: "నా వివరాలు",
        cattle: "పాడి గేదెలు / ఆవులు",
        phone: "ఫోన్ నంబర్",
        village: "గ్రామం",
        bankDetails: "బ్యాంక్ వివరాలు",
        welcomeFarmer: "స్వాగతం రైతు సోదరులారా",
        farmerId: "రైతు ఐడీ",
        print: "స్టేట్‌మెంట్ ప్రింట్"
    }
};

function setLanguage(lang) {
    localStorage.setItem('milk_sys_lang', lang);
    applyLanguage(lang);
}

function applyLanguage(lang) {
    const selected = translations[lang] || translations.en;
    document.querySelectorAll('[data-i18n]').forEach(elem => {
        const key = elem.getAttribute('data-i18n');
        if (selected[key]) {
            elem.innerText = selected[key];
        }
    });

    // Update active button state
    document.querySelectorAll('.lang-btn').forEach(btn => {
        if (btn.getAttribute('data-lang') === lang) {
            btn.classList.add('btn-primary', 'active');
            btn.classList.remove('btn-outline-primary', 'btn-outline-light');
        } else {
            btn.classList.remove('btn-primary', 'active');
            btn.classList.add('btn-outline-primary');
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const savedLang = localStorage.getItem('milk_sys_lang') || 'en';
    applyLanguage(savedLang);
});
