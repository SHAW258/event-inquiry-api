import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch

def create_interview_pdf():
    pdf_filename = r"C:\Users\indrajit\Desktop\event-inquiry-api\Event_Inquiry_API_Interview_QA.pdf"
    
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=letter,
        rightMargin=0.5 * inch,
        leftMargin=0.5 * inch,
        topMargin=0.5 * inch,
        bottomMargin=0.5 * inch
    )

    styles = getSampleStyleSheet()
    
    # Custom Palette
    PRIMARY = colors.HexColor("#1A365D")    # Dark Navy
    SECONDARY = colors.HexColor("#2B6CB0")  # Royal Blue
    ACCENT = colors.HexColor("#2C7A7B")     # Teal
    TEXT_DARK = colors.HexColor("#2D3748")  # Charcoal
    BG_LIGHT = colors.HexColor("#F7FAFC")   # Light Gray
    BORDER_COLOR = colors.HexColor("#E2E8F0")

    # Custom Styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=22,
        leading=26,
        textColor=PRIMARY,
        spaceAfter=4
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12,
        leading=16,
        textColor=SECONDARY,
        spaceAfter=15
    )

    h1_style = ParagraphStyle(
        'Heading1Custom',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=14,
        leading=18,
        textColor=PRIMARY,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    q_style = ParagraphStyle(
        'QuestionStyle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10.5,
        leading=14,
        textColor=SECONDARY,
        spaceBefore=8,
        spaceAfter=3,
        keepWithNext=True
    )

    a_style = ParagraphStyle(
        'AnswerStyle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=TEXT_DARK,
        spaceAfter=8
    )

    code_style = ParagraphStyle(
        'CodeStyle',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=11,
        textColor=colors.HexColor("#1A202C"),
        backColor=BG_LIGHT,
        borderColor=BORDER_COLOR,
        borderWidth=1,
        borderPadding=6,
        spaceBefore=4,
        spaceAfter=8
    )

    story = []

    # Title Banner
    story.append(Paragraph("🎪 Event Inquiry Management API", title_style))
    story.append(Paragraph("Technical Round Interview Q&A & Architecture Preparation Guide", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=PRIMARY, spaceAfter=12))

    # Section 1: Overview & Stack
    story.append(Paragraph("1. Technology Stack & Elevator Pitch", h1_style))
    
    story.append(Paragraph("Q1: Can you provide a high-level overview of the Event Inquiry Management API?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> The Event Inquiry Management API is a stateless Spring Boot 3 RESTful microservice built using Java 17, Spring Security 6, Spring Data JPA, and PostgreSQL. "
        "It provides customer inquiry submission workflows for events (weddings, corporate summits, birthdays), role-based access control (USER vs ADMIN), and strict server-side "
        "IDOR (Insecure Direct Object Reference) authorization checks to prevent unauthorized cross-user data access. The project is fully containerized using Docker Compose and pre-configured for cloud deployment on Railway.",
        a_style
    ))

    # Tech Stack Table
    stack_data = [
        [Paragraph("<b>Component</b>", q_style), Paragraph("<b>Technology Chosen</b>", q_style), Paragraph("<b>Architectural Justification</b>", q_style)],
        [Paragraph("Language & Runtime", a_style), Paragraph("Java 17 (LTS)", a_style), Paragraph("Long-Term Support, enhanced performance, pattern matching, record types.", a_style)],
        [Paragraph("Framework", a_style), Paragraph("Spring Boot 3.2.5", a_style), Paragraph("Enterprise standard, autoconfiguration, embedded Tomcat, Jakarta EE 10.", a_style)],
        [Paragraph("Security", a_style), Paragraph("Spring Security 6 + JJWT", a_style), Paragraph("Stateless JWT authentication, BCrypt password hashing, RBAC annotations.", a_style)],
        [Paragraph("Database & ORM", a_style), Paragraph("PostgreSQL 16 / Hibernate", a_style), Paragraph("ACID compliance, JSONB support, relational indexing, JPA repositories.", a_style)],
        [Paragraph("API Specs", a_style), Paragraph("OpenAPI 3.0 / Swagger UI", a_style), Paragraph("Interactive API testing, Bearer JWT authentication scheme.", a_style)]
    ]
    t_stack = Table(stack_data, colWidths=[1.5*inch, 2.0*inch, 3.5*inch])
    t_stack.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), BG_LIGHT),
        ('GRID', (0,0), (-1,-1), 0.5, BORDER_COLOR),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(t_stack)
    story.append(Spacer(1, 10))

    # Section 2: Database Schema & Entity Design
    story.append(Paragraph("2. Database Schema & Entity Design Q&A", h1_style))

    story.append(Paragraph("Q2: Walk me through your database schema design. What entities exist and how are they related?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> The relational database consists of two primary tables: <b>users</b> and <b>event_inquiries</b>.<br/>"
        "• <b>users</b>: Stores account credentials (<code>id</code>, <code>full_name</code>, <code>email</code> [UNIQUE], <code>password</code> [BCrypt hash], <code>role</code> [ROLE_USER/ROLE_ADMIN], <code>created_at</code>).<br/>"
        "• <b>event_inquiries</b>: Stores inquiry records (<code>id</code>, <code>user_id</code> [FK], <code>customer_name</code>, <code>customer_email</code>, <code>customer_phone</code>, <code>event_type</code>, <code>event_date</code>, <code>location</code>, <code>estimated_budget</code>, <code>guest_count</code>, <code>status</code>, <code>special_requests</code>).<br/>"
        "• <b>Relationship</b>: 1-to-Many relationship (User 1 ──> N EventInquiries) mapped via <code>@ManyToOne(fetch = FetchType.LAZY)</code> with foreign key <code>user_id</code>.",
        a_style
    ))

    story.append(Paragraph("Q3: Why did you choose <code>BigDecimal</code> for the <code>estimated_budget</code> field instead of double or float?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> Binary floating-point types (<code>double</code> and <code>float</code>) suffer from IEEE 754 precision rounding errors during arithmetic operations. In financial and budget domain modeling, exact precision is mandatory. <code>BigDecimal</code> maps cleanly to PostgreSQL <code>NUMERIC(12,2)</code>, guaranteeing exact 2-decimal point precision without cumulative rounding discrepancies.",
        a_style
    ))

    story.append(Paragraph("Q4: What indexes did you create and why?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b><br/>"
        "1. <b>Unique Index on <code>users.email</code></b>: Enables O(1) time complexity lookup during user authentication and registration collision checks.<br/>"
        "2. <b>Foreign Key Index <code>idx_inquiries_user_id</code></b>: Speeds up user-owned inquiry listing queries (<code>SELECT * FROM event_inquiries WHERE user_id = ?</code>).<br/>"
        "3. <b>Filtering Indexes <code>idx_inquiries_status</code> and <code>idx_inquiries_event_type</code></b>: Optimizes administrative filtering and reporting queries.",
        a_style
    ))

    # Section 3: Authentication & JWT Security
    story.append(Paragraph("3. Authentication & Security Architecture Q&A", h1_style))

    story.append(Paragraph("Q5: Explain your JWT authentication flow step-by-step.", q_style))
    story.append(Paragraph(
        "<b>Answer:</b><br/>"
        "1. <b>Login Request</b>: User submits <code>POST /api/v1/auth/login</code> with email & password.<br/>"
        "2. <b>Authentication Manager</b>: Spring Security's <code>AuthenticationManager</code> verifies password hash against stored BCrypt hash.<br/>"
        "3. <b>Token Generation</b>: <code>JwtTokenProvider</code> generates an HMAC SHA-256 signed JWT string containing subject (email), issue date, 24-hour expiration, and granted authorities (<code>ROLE_USER</code> / <code>ROLE_ADMIN</code>).<br/>"
        "4. <b>Request Interception</b>: <code>JwtAuthenticationFilter</code> intercepts subsequent HTTP requests, parses the <code>Authorization: Bearer &lt;token&gt;</code> header, validates signature, loads <code>UserDetails</code>, and populates <code>SecurityContextHolder</code>.",
        a_style
    ))

    # Section 4: IDOR Protection
    story.append(Paragraph("4. Insecure Direct Object Reference (IDOR) Prevention", h1_style))

    story.append(Paragraph("Q6: How did you ensure that User A cannot access or edit User B's protected inquiry simply by changing an ID in the URL?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> Authorization is enforced directly inside the service layer (`EventInquiryServiceImpl`) via an explicit resource ownership validation method (<code>verifyOwnershipOrAdmin</code>). "
        "When any resource method (`getInquiryById`, `updateInquiry`, `deleteInquiry`) is invoked, the service compares the owner ID of the entity against the authenticated user's ID:",
        a_style
    ))

    code_snippet = (
        "private void verifyOwnershipOrAdmin(EventInquiry inquiry, User currentUser) {\n"
        "    boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;\n"
        "    boolean isOwner = Objects.equals(inquiry.getUser().getId(), currentUser.getId());\n\n"
        "    if (!isAdmin && !isOwner) {\n"
        "        throw new UnauthorizedAccessException(\"Access denied: You do not have permission to access or modify this inquiry.\");\n"
        "    }\n"
        "}"
    )
    story.append(Paragraph(code_snippet.replace("\n", "<br/>").replace(" ", "&nbsp;"), code_style))

    story.append(Paragraph(
        "If a non-admin user attempts an IDOR attack by altering the URL path parameter, the method throws <code>UnauthorizedAccessException</code>, which <code>GlobalExceptionHandler</code> catches and returns as <b>HTTP 403 Forbidden</b>. This was verified with automated integration tests in <code>SecurityAuthorizationTest.java</code>.",
        a_style
    ))

    # Section 5: API Architecture & Error Handling
    story.append(Paragraph("5. API Architecture & Standardized Error Handling", h1_style))

    story.append(Paragraph("Q7: How is global error handling implemented across the REST APIs?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> We use <code>@RestControllerAdvice</code> in <code>GlobalExceptionHandler.java</code> to capture all exceptions framework-wide. Validation errors (`MethodArgumentNotValidException`) extract field errors into a key-value map, while domain exceptions map to appropriate HTTP status codes (<code>400 Bad Request</code>, <code>401 Unauthorized</code>, <code>403 Forbidden</code>, <code>404 Not Found</code>, <code>409 Conflict</code>).",
        a_style
    ))

    # Section 6: Testing & Deployment
    story.append(Paragraph("6. Testing & Deployment Strategy", h1_style))

    story.append(Paragraph("Q8: How did you structure your test suite?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b> The project includes 9 comprehensive JUnit 5 tests:<br/>"
        "• <b>`SecurityAuthorizationTest`</b>: Integration test using <code>MockMvc</code> and <code>@WithMockUser</code> to verify IDOR protection and role enforcement.<br/>"
        "• <b>`AuthControllerTest`</b>: MockMvc tests verifying registration and JWT login response contracts.<br/>"
        "• <b>`EventInquiryServiceTest`</b>: Unit tests covering service business logic and repository interactions.<br/>"
        "Tests run against an isolated H2 database profile (`test`), completing build execution with zero failures.",
        a_style
    ))

    story.append(Paragraph("Q9: How is the application deployed?", q_style))
    story.append(Paragraph(
        "<b>Answer:</b><br/>"
        "1. <b>Local Dev Mode</b>: Uses embedded H2 database (<code>dev</code> profile) running on <code>http://localhost:8080</code>.<br/>"
        "2. <b>Docker Compose</b>: Multi-container setup with PostgreSQL 16 container (`docker-compose up --build`).<br/>"
        "3. <b>Railway Cloud</b>: Pre-configured <code>railway.json</code> and multi-stage <code>Dockerfile</code> with dynamic <code>$PORT</code> binding.",
        a_style
    ))

    # Build Document
    doc.build(story)
    print(f"PDF created successfully at: {pdf_filename}")

if __name__ == "__main__":
    create_interview_pdf()
