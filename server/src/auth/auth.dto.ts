import { IsJWT, IsNotEmpty, IsString } from 'class-validator';

export class LoginDto {
  @IsString()
  @IsNotEmpty()
  username: string;

  @IsString()
  @IsNotEmpty()
  password: string;
}

export class TokenRefreshDto {
  @IsJWT()
  refreshToken: string;
}

export interface TokensResponse {
  accessToken: string;
  refreshToken: string;
}
