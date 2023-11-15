package com.madeeasy.vo;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private String toEmail;
    private String subject;
    private String text;
}
