package com.red.team.taskvisionapp.service.impl;

import org.springframework.web.util.HtmlUtils;

import java.time.Year;
import java.util.Locale;
import java.util.Map;

final class WelcomeEmailTemplate {

    private WelcomeEmailTemplate() {
    }

    static String plainText(String name, String email, String role, String loginUrl) {
        String roleLabel = roleLabel(role);
        return """
                Halo, %s!

                Selamat datang di Task Vision. Akun Anda sudah berhasil dibuat dan siap digunakan untuk berkolaborasi bersama tim.

                Email: %s
                Peran: %s
                %s

                Yang bisa dilakukan di Task Vision:
                - Memantau project dan progres tim dalam satu dashboard.
                - Mengelola task dengan alur approval dan feedback yang jelas.
                - Mengunduh laporan project dalam format Excel.

                Masuk ke Task Vision:
                %s

                Password tidak ditampilkan di email ini. Gunakan password yang diberikan administrator workspace Anda.
                Jika Anda tidak merasa memiliki akun ini, abaikan email ini atau hubungi administrator.

                (c) %d Task Vision - Enigma Red Team
                Email ini dikirim otomatis. Mohon tidak membalas email ini.
                """.formatted(
                safe(name),
                safe(email),
                roleLabel,
                roleDescription(role),
                safe(loginUrl),
                Year.now().getValue()
        );
    }

    static String html(String name, String email, String role, String loginUrl) {
        Map<String, String> values = Map.of(
                "NAME", HtmlUtils.htmlEscape(safe(name)),
                "EMAIL", HtmlUtils.htmlEscape(safe(email)),
                "ROLE", HtmlUtils.htmlEscape(roleLabel(role)),
                "ROLE_DESCRIPTION", HtmlUtils.htmlEscape(roleDescription(role)),
                "LOGIN_URL", HtmlUtils.htmlEscape(safe(loginUrl)),
                "YEAR", String.valueOf(Year.now().getValue())
        );

        return render("""
                <!doctype html>
                <html lang="id">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="color-scheme" content="light">
                  <meta name="supported-color-schemes" content="light">
                  <title>Selamat datang di Task Vision</title>
                  <style>
                    body { margin: 0; padding: 0; background: #f4f5fb; }
                    table { border-collapse: collapse; }
                    a { text-decoration: none; }
                    @media only screen and (max-width: 620px) {
                      .email-shell { width: 100% !important; }
                      .email-body { padding: 30px 24px 26px !important; }
                      .email-footer { padding: 18px 24px 22px !important; }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background:#f4f5fb;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;mso-hide:all;">
                    Akun Task Vision Anda sudah aktif. Mulai kelola project dan task bersama tim.
                  </div>
                  <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="width:100%;background:#f4f5fb;">
                    <tr>
                      <td align="center" style="padding:32px 16px;">
                        <table role="presentation" class="email-shell" width="620" cellpadding="0" cellspacing="0" border="0" style="width:100%;max-width:620px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 18px 50px rgba(30,41,59,0.12);">
                          <tr>
                            <td style="padding:0;">
                              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color:#4f46e5;background-image:linear-gradient(135deg,#4f46e5 0%,#7c3aed 58%,#06b6d4 100%);">
                                <tr>
                                  <td style="padding:30px 36px;">
                                    <table role="presentation" cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td width="48" valign="middle">
                                          <div style="width:46px;height:46px;border-radius:14px;background:rgba(255,255,255,0.16);border:1px solid rgba(255,255,255,0.24);color:#ffffff;font-size:15px;font-weight:700;line-height:46px;text-align:center;letter-spacing:0.5px;">TV</div>
                                        </td>
                                        <td style="padding-left:14px;color:#ffffff;font-size:19px;font-weight:700;letter-spacing:-0.2px;">
                                          Task Vision
                                          <div style="margin-top:3px;color:rgba(255,255,255,0.8);font-size:12px;font-weight:500;letter-spacing:0.2px;">Project Management Hub</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td class="email-body" style="padding:40px 42px 34px;">
                              <span style="display:inline-block;padding:6px 12px;border-radius:999px;background:#eef2ff;color:#4338ca;font-size:11px;font-weight:700;letter-spacing:0.7px;text-transform:uppercase;">Akun aktif</span>
                              <h1 style="margin:18px 0 10px;color:#111827;font-size:28px;font-weight:700;line-height:1.25;letter-spacing:-0.7px;">Halo, {{NAME}}!</h1>
                              <p style="margin:0;color:#4b5563;font-size:15px;line-height:1.75;">
                                Selamat datang di <strong style="color:#111827;">Task Vision</strong>. Akun Anda sudah berhasil dibuat dan siap digunakan untuk berkolaborasi bersama tim.
                              </p>

                              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" style="margin-top:26px;background:#f8f9ff;border:1px solid #e5e7f5;border-radius:14px;">
                                <tr>
                                  <td style="padding:18px 20px;">
                                    <div style="color:#6b7280;font-size:11px;font-weight:700;letter-spacing:0.8px;text-transform:uppercase;">Detail akun</div>
                                    <div style="margin-top:10px;color:#374151;font-size:14px;line-height:1.7;">
                                      <span style="color:#6b7280;">Email</span><br>
                                      <strong style="color:#111827;">{{EMAIL}}</strong>
                                    </div>
                                    <div style="margin-top:10px;color:#374151;font-size:14px;line-height:1.7;">
                                      <span style="color:#6b7280;">Peran</span><br>
                                      <strong style="color:#111827;">{{ROLE}}</strong>
                                    </div>
                                    <div style="margin-top:8px;color:#6b7280;font-size:13px;line-height:1.65;">{{ROLE_DESCRIPTION}}</div>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:28px 0 4px;color:#111827;font-size:14px;font-weight:700;">Yang bisa dilakukan di Task Vision</p>
                              <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td width="30" valign="top" style="padding-top:10px;">
                                    <div style="width:22px;height:22px;border-radius:50%;background:#ecfdf5;color:#059669;font-size:12px;font-weight:700;line-height:22px;text-align:center;">&#10003;</div>
                                  </td>
                                  <td style="padding:8px 0 0 10px;color:#4b5563;font-size:14px;line-height:1.65;">Pantau project dan progres tim dalam satu dashboard.</td>
                                </tr>
                                <tr>
                                  <td width="30" valign="top" style="padding-top:10px;">
                                    <div style="width:22px;height:22px;border-radius:50%;background:#ecfdf5;color:#059669;font-size:12px;font-weight:700;line-height:22px;text-align:center;">&#10003;</div>
                                  </td>
                                  <td style="padding:8px 0 0 10px;color:#4b5563;font-size:14px;line-height:1.65;">Kelola task dengan alur approval dan feedback yang jelas.</td>
                                </tr>
                                <tr>
                                  <td width="30" valign="top" style="padding-top:10px;">
                                    <div style="width:22px;height:22px;border-radius:50%;background:#ecfdf5;color:#059669;font-size:12px;font-weight:700;line-height:22px;text-align:center;">&#10003;</div>
                                  </td>
                                  <td style="padding:8px 0 0 10px;color:#4b5563;font-size:14px;line-height:1.65;">Unduh laporan project dalam format Excel.</td>
                                </tr>
                              </table>

                              <table role="presentation" cellpadding="0" cellspacing="0" border="0" style="margin-top:30px;">
                                <tr>
                                  <td style="border-radius:12px;background:#4f46e5;">
                                    <a href="{{LOGIN_URL}}" target="_blank" rel="noopener" style="display:inline-block;padding:14px 26px;border-radius:12px;color:#ffffff;font-size:14px;font-weight:700;">Masuk ke Task Vision &nbsp;&rarr;</a>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:20px 0 0;color:#94a3b8;font-size:12px;line-height:1.7;">
                                Jika tombol tidak berfungsi, buka tautan berikut:<br>
                                <a href="{{LOGIN_URL}}" target="_blank" rel="noopener" style="color:#4f46e5;word-break:break-all;">{{LOGIN_URL}}</a>
                              </p>

                              <div style="margin-top:30px;padding-top:22px;border-top:1px solid #edf0f6;">
                                <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.75;">
                                  Password tidak ditampilkan di email ini. Gunakan password yang diberikan administrator workspace Anda.
                                  Jika Anda tidak merasa memiliki akun ini, abaikan email ini atau hubungi administrator.
                                </p>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td class="email-footer" style="padding:20px 42px 26px;background:#f8fafc;border-top:1px solid #eef1f6;">
                              <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.7;">
                                &copy; {{YEAR}} Task Vision &middot; Enigma Red Team<br>
                                Email ini dikirim otomatis. Mohon tidak membalas email ini.
                              </p>
                            </td>
                          </tr>
                        </table>
                        <p style="margin:18px 0 0;color:#9ca3af;font-size:11px;line-height:1.6;">
                          Butuh bantuan? Hubungi administrator workspace Anda.
                        </p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """, values);
    }

    private static String roleLabel(String role) {
        return switch (normalizeRole(role)) {
            case "ADMIN" -> "Administrator";
            case "PROJECT_MANAGER" -> "Project Manager";
            default -> "Member";
        };
    }

    private static String roleDescription(String role) {
        return switch (normalizeRole(role)) {
            case "ADMIN" -> "Akses penuh untuk mengelola user, project, dan memantau seluruh aktivitas tim.";
            case "PROJECT_MANAGER" -> "Kelola project, bagi task ke anggota tim, serta lakukan review dan approval.";
            default -> "Kerjakan task, kirim hasil untuk approval, dan terima feedback dari project manager.";
        };
    }

    private static String normalizeRole(String role) {
        return role == null ? "MEMBER" : role.trim().toUpperCase(Locale.ROOT);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String render(String template, Map<String, String> values) {
        StringBuilder result = new StringBuilder(template.length());
        int cursor = 0;
        while (cursor < template.length()) {
            int start = template.indexOf("{{", cursor);
            if (start < 0) {
                result.append(template, cursor, template.length());
                break;
            }
            int end = template.indexOf("}}", start + 2);
            if (end < 0) {
                result.append(template, cursor, template.length());
                break;
            }
            result.append(template, cursor, start);
            String key = template.substring(start + 2, end);
            result.append(values.getOrDefault(key, ""));
            cursor = end + 2;
        }
        return result.toString();
    }
}
