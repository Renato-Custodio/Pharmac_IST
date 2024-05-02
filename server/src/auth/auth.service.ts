import { Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { compare } from 'bcrypt';
import { randomUUID } from 'crypto';
import { PrismaService } from 'src/core/prisma.service';
import { LoginDto, TokenRefreshDto, TokensResponse } from './auth.dto';

interface RefreshTokenPayload {
  id: string;
  tokenId: string;
}

@Injectable()
export class AuthService {
  constructor(
    private prismaService: PrismaService,
    private jwtService: JwtService,
  ) {}

  private async validateUser(loginDto: LoginDto): Promise<string> {
    const account = await this.prismaService.account.findUnique({
      where: {
        username: loginDto.username,
      },
    });

    if (!account) throw new UnauthorizedException('Invalid credentials');

    const isMatch = await compare(loginDto.password, account.password);

    if (!isMatch) throw new UnauthorizedException('Invalid credentials');

    return account.id;
  }

  private async createAccessToken(userId: string) {
    return this.jwtService.sign({ id: userId }, { expiresIn: '15m' });
  }

  private async createRefreshToken(userId: string) {
    const tokenId = randomUUID();

    await this.prismaService.account.update({
      where: {
        id: userId,
      },
      data: {
        refreshTokens: {
          push: tokenId,
        },
      },
    });

    const payload: RefreshTokenPayload = {
      id: userId,
      tokenId: tokenId,
    };

    return this.jwtService.sign(payload, { expiresIn: '7d' });
  }

  private decodeRefreshToken(token: string): RefreshTokenPayload {
    try {
      return this.jwtService.verify(token);
    } catch (error) {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }

  private async replaceRefreshToken(refreshPayload: RefreshTokenPayload) {
    // Invalidate old token
    await this.prismaService.$transaction(async (tx) => {
      const oldTokenList = await tx.account.findUnique({
        where: {
          id: refreshPayload.id,
        },
        select: {
          refreshTokens: true,
        },
      });

      if (!oldTokenList)
        throw new UnauthorizedException('Invalid refresh token');

      const oldIndex = oldTokenList.refreshTokens.indexOf(
        refreshPayload.tokenId,
      );

      if (oldIndex === -1)
        throw new UnauthorizedException('Invalid refresh token');

      await this.prismaService.account.update({
        where: {
          id: refreshPayload.id,
        },
        data: {
          refreshTokens: oldTokenList.refreshTokens.splice(oldIndex, 1),
        },
      });
    });

    return this.createRefreshToken(refreshPayload.id);
  }

  async login(loginDto: LoginDto): Promise<TokensResponse> {
    const userId = await this.validateUser(loginDto);
    const accessToken = await this.createAccessToken(userId);
    const refreshToken = await this.createRefreshToken(userId);

    return {
      accessToken,
      refreshToken,
    };
  }

  async refresh(refreshDto: TokenRefreshDto): Promise<TokensResponse> {
    const decodedToken = this.decodeRefreshToken(refreshDto.refreshToken);
    const accessToken = await this.replaceRefreshToken(decodedToken);
    const refreshToken = await this.createAccessToken(decodedToken.id);

    return {
      accessToken,
      refreshToken,
    };
  }
}
