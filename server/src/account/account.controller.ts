import { Body, Controller, Get, Param, Post, Put } from '@nestjs/common';
import { AccountService } from './account.service';
import { Account } from '@prisma/client';
import { LoginDto, TokenRefreshDto } from 'src/auth/auth.dto';
import { AuthService } from 'src/auth/auth.service';

@Controller('api/account')
export class AccountController {
  constructor(
    private readonly accountService: AccountService,
    private readonly authService: AuthService,
  ) {}

  @Post('auth/createAccount')
  async createAccount(@Body() loginDto: LoginDto) {
    return await this.authService.createAccount(loginDto);
  }

  @Post('auth/login')
  async login(@Body() loginDto: LoginDto) {
    return await this.authService.login(loginDto);
  }

  @Post('auth/refresh')
  async refresh(@Body() refreshDto: TokenRefreshDto) {
    return await this.authService.refresh(refreshDto);
  }

  @Get()
  async findAll(): Promise<Account[]> {
    return await this.accountService.getUsers();
  }

  @Put(':accountId/addFavorite/:pharmacyId')
  async addFavorite(
    @Param('accountId') accountId: string,
    @Param('pharmacyId') pharmacyId: string,
  ): Promise<Account> {
    return await this.accountService.addFavoritePharmacie(
      accountId,
      pharmacyId,
    );
  }

  @Put(':accountId/removeFavorite/:pharmacyId')
  async removeFavorite(
    @Param('accountId') accountId: string,
    @Param('pharmacyId') pharmacyId: string,
  ): Promise<Account> {
    return await this.accountService.removeFavoritePharmacy(
      accountId,
      pharmacyId,
    );
  }
}
