import { Injectable } from '@nestjs/common';
import { Account } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';

@Injectable()
export class AccountService {
  constructor(private prismaService: PrismaService) {}

  async getUsers(): Promise<Account[]> {
    return await this.prismaService.account.findMany();
  }
}
