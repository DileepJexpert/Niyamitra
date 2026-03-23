-- Anupalan Rule Engine: Compliance Rules Table
CREATE TABLE IF NOT EXISTS anupalan_rules.anupalan_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(30) NOT NULL,
    authority VARCHAR(255) NOT NULL,
    applicable_states TEXT,
    applicable_nic_codes TEXT,
    industry_categories TEXT,
    renewal_period_months INTEGER,
    penalty_description TEXT,
    required_documents JSONB,
    portal_url VARCHAR(500),
    description_en TEXT,
    description_hi TEXT
);

CREATE INDEX idx_anupalan_rules_category ON anupalan_rules.anupalan_rules(category);

-- Seed data: UP compliance rules for Phase 1
INSERT INTO anupalan_rules.anupalan_rules (name, category, authority, applicable_states, applicable_nic_codes, industry_categories, renewal_period_months, penalty_description, portal_url, description_en, description_hi) VALUES
('UPPCB Consent to Operate (CTO)', 'POLLUTION', 'UP Pollution Control Board', 'UP', '10,11,12,13,14,15,16,17,20,21,22,23,24,25', 'RED,ORANGE', 60, 'Closure notice, penalty up to Rs 1 lakh/day under Water Act 1974 Section 33A', 'https://upocmms.nic.in', 'Consent to Operate from UPPCB is mandatory for all Red/Orange category industries in UP. Must be renewed every 5 years.', 'UPPCB से संचालन की सहमति (CTO) UP में सभी Red/Orange श्रेणी के उद्योगों के लिए अनिवार्य है। हर 5 साल में नवीनीकरण आवश्यक।'),

('Fire NOC', 'FIRE', 'UP Fire Service', 'UP,HR,DL', '10,11,12,13,14,15,16,17,20,21,22,23,24,25', 'RED,ORANGE,GREEN', 12, 'Factory closure order, penalty under Fire Services Act', 'https://firenoc.up.gov.in', 'Fire No Objection Certificate required for all manufacturing units. Annual renewal.', 'अग्नि अनापत्ति प्रमाणपत्र सभी विनिर्माण इकाइयों के लिए आवश्यक है। वार्षिक नवीनीकरण।'),

('Factory License', 'LABOUR', 'Chief Inspector of Factories', 'UP,HR,DL,RJ', '10,11,12,13,14,15,16,17,20,21,22,23,24,25', 'RED,ORANGE,GREEN,WHITE', 12, 'Penalty under Factories Act 1948, Section 92 — up to Rs 2 lakh', NULL, 'Factory License under Factories Act 1948 is mandatory for all manufacturing units with 10+ workers.', 'फैक्ट्री अधिनियम 1948 के तहत फैक्ट्री लाइसेंस 10+ श्रमिकों वाली सभी विनिर्माण इकाइयों के लिए अनिवार्य है।'),

('Hazardous Waste Authorization', 'POLLUTION', 'UP Pollution Control Board', 'UP', '20,21,22,23,24,25', 'RED', 60, 'Criminal prosecution under Hazardous Waste Management Rules 2016', 'https://upocmms.nic.in', 'Authorization for generation, storage, and disposal of hazardous waste. Required for Red category industries.', 'खतरनाक अपशिष्ट के उत्पादन, भंडारण और निपटान के लिए प्राधिकरण। Red श्रेणी उद्योगों के लिए आवश्यक।'),

('Boiler Registration', 'LABOUR', 'Chief Inspector of Boilers', 'UP,HR,DL,RJ', '10,11,13,17,20,24', 'RED,ORANGE', 12, 'Penalty under Indian Boilers Act 1923, factory closure', NULL, 'Boiler registration and annual inspection certificate required for all units operating steam boilers.', 'स्टीम बॉयलर संचालित करने वाली सभी इकाइयों के लिए बॉयलर पंजीकरण और वार्षिक निरीक्षण प्रमाणपत्र आवश्यक।'),

('UPPCB Consent to Establish (CTE)', 'POLLUTION', 'UP Pollution Control Board', 'UP', '10,11,12,13,14,15,16,17,20,21,22,23,24,25', 'RED,ORANGE', 0, 'Cannot begin construction without CTE. Penalty under Water/Air Act.', 'https://upocmms.nic.in', 'Consent to Establish must be obtained before starting construction of any industrial unit in UP.', 'UP में किसी भी औद्योगिक इकाई का निर्माण शुरू करने से पहले स्थापना की सहमति (CTE) प्राप्त करना अनिवार्य है।');
