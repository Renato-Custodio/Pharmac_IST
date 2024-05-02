import { Injectable } from '@nestjs/common';
import { Account } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';

@Injectable()
export class AccountService {
  constructor(private prismaService: PrismaService) {}

  async getUsers(): Promise<Account[]> {
    return await this.prismaService.account.findMany();
  }

  async getUser(username: string): Promise<Account | null> {
    const account = await this.prismaService.account.findFirst({
      where: {
        username: {
          equals: username,
        },
      },
    });

    return account ?? null;
  }
}
